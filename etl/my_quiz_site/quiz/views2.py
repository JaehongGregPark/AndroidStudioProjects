# my_quiz_site/quiz/views.py
from django.contrib.admin.views.decorators import staff_member_required
from django.contrib import messages
import os
from django.core.files import File
from django.shortcuts import render, redirect, get_object_or_404
from rest_framework import viewsets
from .models import Exam, Question, QuestionImage, Choice
from .serializers import QuestionSerializer
import pytesseract
import re
from PIL import Image
import cv2
import numpy as np

# 1. 기존의 API 전용 뷰셋 (데이터 통신용)
class QuestionViewSet(viewsets.ModelViewSet):
    queryset = Question.objects.all().order_by('id') # ID 순으로 정렬
    serializer_class = QuestionSerializer

# 2. 새로 추가하는 웹 화면용 함수 (HTML 렌더링용)
def question_list(request):

    categories = Category.objects.all()
    exams = Exam.objects.all()
    
    category_id = request.GET.get('category')
    exam_id = request.GET.get('exam')

    questions = Question.objects.all()
    # 필터링 로직
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

# 경로에 역슬래시(\)가 있으므로 문자열 앞에 r을 붙이거나 슬래시(/)를 사용하세요.
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

@staff_member_required
def admin_manual(request):
    exams = Exam.objects.all()  # 시험지 목록을 가져와서
    return render(request, 'admin/quiz/admin_manual.html', {'exams': exams}) # 템플릿에 전달!

@staff_member_required
def admin_answer_ocr_update(request):
    if request.method == "POST" and request.FILES.get('answer_sheet'):
        exam_id = request.POST.get('exam_id')
        exam = get_object_or_404(Exam, id=exam_id)
        image_file = request.FILES['answer_sheet']
        
        # 1. 이미지 읽기 및 선 제거 (기존 로직 유지)
        file_bytes = np.asarray(bytearray(image_file.read()), dtype=np.uint8)
        img = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]

        # 가로/세로 선 제거
        for k_size in [(40, 1), (1, 40)]:
            kernel = cv2.getStructuringElement(cv2.MORPH_RECT, k_size)
            lines = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel, iterations=2)
            cnts = cv2.findContours(lines, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)[0]
            for c in cnts:
                cv2.drawContours(thresh, [c], -1, (0, 0, 0), 5)

        result_img = 255 - thresh
        text = pytesseract.image_to_string(result_img, config=r'--oem 3 --psm 6 -l kor+eng')

        # 2. [강력 보정] 문자열 치환 로직
        # 로그 분석 결과: 0->1, 8->3, @->4, 괄호->마침표 로 치환 시 생존율이 가장 높습니다.
        processed_text = text.replace(')', '.').replace('(', '.')
        
        # 3. 정규표현식 확장
        # 숫자 + (마침표나 공백) + (정답후보군: 숫자 혹은 @, 0, 8)
        # 예: "1.8", "34. 4", "5.@ " 등을 모두 추출
        pattern = r'(\d{1,3})\s*[\.\s]*\s*([0-9@])'
        matches = re.findall(pattern, processed_text)
        
        update_count = 0
        updated_nums = set()
        
        # 정답 기호 매핑 테이블 (현재 OCR 결과 기준 최적화)
        ans_map = {
            '0': '1', # ① 오인식
            '8': '3', # ③ 오인식 
            '@': '4', # ④ 오인식 (주로 마지막 보기)
        }

        for q_num, raw_ans in matches:
            num = int(q_num)
            
            # 이미 처리했거나 범위를 벗어나면 패스
            if num in updated_nums or not (1 <= num <= 100):
                continue

            # 정답값 결정
            final_ans = ans_map.get(raw_ans, raw_ans)
            
            # 최종 정답이 1~4 사이인 경우만 저장
            if final_ans in ['1', '2', '3', '4']:
                question = Question.objects.filter(exam=exam, number=num).first()
                if question:
                    question.answer = final_ans
                    question.save()
                    update_count += 1
                    updated_nums.add(num)

        if update_count > 0:
            messages.success(request, f"선 제거 및 패턴 보정을 통해 {update_count}개의 정답을 업데이트했습니다.")
        else:
            messages.error(request, "분석 결과가 유효하지 않습니다. 이미지를 더 밝게 찍어주세요.")

        return redirect('admin_manual')