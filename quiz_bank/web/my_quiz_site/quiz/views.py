from django.contrib.admin.views.decorators import staff_member_required
from django.contrib import messages
from django.core.files import File
from rest_framework import viewsets
from .serializers import QuestionSerializer
from django.shortcuts import render, redirect, get_object_or_404
import json
import os
import re
from google.cloud import vision
from .models import Exam, Question, QuestionImage, Choice

class QuestionViewSet(viewsets.ReadOnlyModelViewSet):
    # 모든 문제를 가져오되, 번호 순서대로 정렬합니다.
    queryset = Question.objects.all().order_by('number')
    serializer_class = QuestionSerializer

def question_list(request):
    category_id = request.GET.get('category')
    exam_id = request.GET.get('exam')

    categories = Category.objects.all()
    exams = Exam.objects.all()
    
    # 필터링 로직
    questions = Question.objects.all()
    if category_id:
        questions = questions.filter(exam__category_id=category_id)
        exams = exams.filter(category_id=category_id)
    if exam_id:
        questions = questions.filter(exam_id=exam_id)

    return render(request, 'quiz/question_list.html', {
        'questions': questions,
        'categories': categories,
        'exams': exams,
        'selected_category': category_id,
        'selected_exam': exam_id,
    })

def exam_bulk_edit(request, exam_id):
    exam = get_object_or_404(Exam, id=exam_id)
    questions = exam.questions.all().order_by('number')

    if request.method == "POST":
        for question in questions:
            # HTML input name을 'answer_{{question.id}}' 형태로 받을 예정
            new_answer = request.POST.get(f'answer_{question.id}')
            new_number = request.POST.get(f'number_{question.id}')
            
            if new_answer is not None:
                question.answer = new_answer
            if new_number is not None:
                question.number = new_number
            question.save()
            
        return redirect('admin:quiz_exam_changelist') # 저장 후 다시 관리자 목록으로

    return render(request, 'admin/quiz/bulk_edit.html', {
        'exam': exam,
        'questions': questions,
    })

@staff_member_required
def admin_image_matcher(request):
    if request.method == "POST":
        image_dir = request.POST.get('image_dir') # 관리자가 입력한 서버 내 폴더 경로
        
        if not os.path.exists(image_dir):
            messages.error(request, f"폴더를 찾을 수 없습니다: {image_dir}")
            return redirect('admin_manual')

        files = [f for f in os.listdir(image_dir) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]
        success_count = 0
        
        for file_name in files:
            try:
                name_part = os.path.splitext(file_name)[0]
                parts = name_part.split('_') # [시험지ID, 문제번호, (선택)보기번호]

                exam_id = int(parts[0])
                q_num = int(parts[1])
                question = Question.objects.get(exam_id=exam_id, number=q_num)

                # 보기 이미지 처리 (예: 5_45_1.jpg)
                if len(parts) == 3:
                    choice_idx = int(parts[2]) - 1
                    choices = question.choices.all().order_by('id')
                    if choice_idx < len(choices):
                        target_choice = choices[choice_idx]
                        with open(os.path.join(image_dir, file_name), 'rb') as f:
                            target_choice.image_file.save(file_name, File(f), save=True)
                        success_count += 1
                
                # 지문 이미지 처리 (예: 5_45.jpg)
                else:
                    with open(os.path.join(image_dir, file_name), 'rb') as f:
                        q_image = QuestionImage(question=question)
                        q_image.image_file.save(file_name, File(f), save=True)
                    success_count += 1
            except Exception:
                continue

        messages.success(request, f"총 {success_count}개의 이미지가 성공적으로 매칭되었습니다.")
        return redirect('admin_manual') # 기존 운영 메뉴얼 페이지로 리다이렉트

    return redirect('admin_manual')

@staff_member_required
def admin_manual(request):
    return render(request, 'admin/quiz/admin_manual.html')

# 1. 구글 인증 키 설정 (본인의 JSON 키 파일 경로로 수정하세요)
# 예: os.path.join(settings.BASE_DIR, 'keys', 'your-google-key.json')
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "C:/Users/USER/Documents/project/web/my_quiz_site/keys/service-account-key.json"

def admin_answer_ocr_update(request):
    if request.method == "POST" and request.FILES.get('answer_sheet'):
        exam_id = request.POST.get('exam_id')
        exam = get_object_or_404(Exam, id=exam_id)
        image_file = request.FILES['answer_sheet']
        
        # 2. Vision API 클라이언트 초기화
        client = vision.ImageAnnotatorClient()

        # 3. 이미지 읽기
        content = image_file.read()
        image = vision.Image(content=content)

        # 4. 텍스트 감지 실행 (DOCUMENT_TEXT_DETECTION은 문서/표 인식에 최적화됨)
        response = client.document_text_detection(image=image)
        full_text = response.full_text_annotation.text

        print("--- [Google Vision] 인식 결과 ---")
        print(full_text)
        print("-------------------------------")

        # 5. 데이터 보정 및 추출
        # 구글은 원문자를 매우 잘 읽으므로 유니코드 대응만 해주면 됩니다.
        circle_map = {
            '①': '1', '②': '2', '③': '3', '④': '4', '⑤': '5',
            '①': '1', '②': '2', '③': '3', '④': '4', '⑤': '5', # 중복 방지
        }
        
        processed_text = full_text
        for target, val in circle_map.items():
            processed_text = processed_text.replace(target, val)

        # 6. 정규표현식으로 (문제번호) (정답) 추출
        # 구글은 보통 "1. 3" 또는 "1 3" 형태로 아주 깔끔하게 반환합니다.
        matches = re.findall(r'(\d{1,3})\s*[\.\s]*\s*([1-5])', processed_text)
        
        update_count = 0
        updated_nums = set()
        logs = []

        for q_num, q_ans in matches:
            num = int(q_num)
            ans = q_ans

            if num in updated_nums or not (1 <= num <= 100):
                continue

            question = Question.objects.filter(exam=exam, number=num).first()
            if question:
                question.answer = ans
                question.save()
                update_count += 1
                updated_nums.add(num)
                logs.append(f"{num}:{ans}")

        if response.error.message:
            messages.error(request, f"Google API 오류: {response.error.message}")
        elif update_count > 0:
            print(f"Final Updates: {sorted(logs, key=lambda x: int(x.split(':')[0]))}")
            messages.success(request, f"Google Vision을 통해 {update_count}개의 정답을 정확히 업데이트했습니다!")
        else:
            messages.warning(request, "이미지에서 정답 패턴을 찾지 못했습니다.")

        return redirect('admin_manual')