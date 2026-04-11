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
import easyocr

# 모델을 미리 로드해둡니다 (함수 안에 넣으면 매번 실행 시 느려집니다)
# 한국어(ko)와 영어(en) 모델 사용
reader = easyocr.Reader(['ko', 'en'])

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
        
        # 1. 이미지 읽기
        file_bytes = np.asarray(bytearray(image_file.read()), dtype=np.uint8)
        img = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)

        # 2. EasyOCR 실행 (이미지 경로 대신 넘파이 배열 직접 전달)
        # detail=0으로 설정하면 텍스트만 리스트로 반환합니다.
        results = reader.readtext(img, detail=0)
        
        # 리스트를 하나의 문자열로 합칩니다.
        full_text = " ".join(results)
        
        print("--- [EasyOCR] 인식 결과 ---")
        print(full_text)
        print("--------------------------")

        # 3. 오인식 보정 (EasyOCR은 원문자를 훨씬 잘 읽지만, 혹시 모를 대비)
        replace_map = {
            '①': '1', '②': '2', '③': '3', '④': '4', '⑤': '5',
            '①': '1', '②': '2', '③': '3', '④': '4', # 유니코드 중복 대응
        }
        for target, val in replace_map.items():
            full_text = full_text.replace(target, val)

        # 4. 정규표현식 추출
        # EasyOCR은 보통 '1 3' 또는 '1 . 3' 형태로 끊어 읽는 경우가 많습니다.
        matches = re.findall(r'(\d{1,3})\s*[\.\s]*\s*([1-5])', full_text)
        
        update_count = 0
        updated_nums = set()

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

        if update_count > 0:
            messages.success(request, f"EasyOCR로 {update_count}개의 정답을 업데이트했습니다.")
        else:
            messages.warning(request, "정답을 인식하지 못했습니다. 이미지 화질을 확인해주세요.")

        return redirect('admin_manual')