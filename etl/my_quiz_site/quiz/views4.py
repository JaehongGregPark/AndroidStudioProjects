# my_quiz_site/quiz/views.py

from django.contrib.admin.views.decorators import staff_member_required
from django.contrib import messages
import os
from django.core.files import File
from django.shortcuts import render, redirect, get_object_or_404
from rest_framework import viewsets

from .models import Exam, Question, QuestionImage, Choice, Category
from .serializers import QuestionSerializer

import pytesseract
import re
import cv2
import numpy as np
import easyocr

# -----------------------------------------------------------------------------------
# 🔥 0. OCR 모델 사전 로딩 (성능 핵심)
# -----------------------------------------------------------------------------------
# 매 요청마다 생성하면 매우 느려짐 → 서버 시작 시 1회만 로딩
reader = easyocr.Reader(['ko', 'en'])


# -----------------------------------------------------------------------------------
# 🔧 1. OCR 전처리 함수 (정확도 핵심)
# -----------------------------------------------------------------------------------
def preprocess_for_ocr(img):
    """
    OCR 정확도를 높이기 위한 전처리 파이프라인

    ✔ 효과
    - 글자 대비 증가
    - 노이즈 제거
    - 작은 글씨 인식 향상

    단계:
    1. Grayscale
    2. 해상도 확대
    3. Blur (노이즈 제거)
    4. Adaptive Threshold (이진화)
    5. Morphology (글자 강화)
    """

    # 1️⃣ 컬러 → 흑백
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # 2️⃣ 해상도 2배 (작은 글씨 대응)
    gray = cv2.resize(gray, None, fx=2, fy=2, interpolation=cv2.INTER_CUBIC)

    # 3️⃣ 노이즈 제거
    blur = cv2.GaussianBlur(gray, (5, 5), 0)

    # 4️⃣ 이진화 (글자 강조)
    thresh = cv2.adaptiveThreshold(
        blur,
        255,
        cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
        cv2.THRESH_BINARY,
        11,
        2
    )

    # 5️⃣ 글자 연결/강화
    kernel = np.ones((2, 2), np.uint8)
    morph = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)

    return morph


# -----------------------------------------------------------------------------------
# 🔄 2. 기울기 자동 보정 (Deskew)
# -----------------------------------------------------------------------------------
def deskew(img):
    """
    이미지 기울기 자동 보정

    ✔ 사진 찍은 시험지에서 매우 중요
    ✔ 기울어지면 OCR 정확도 급락
    """

    coords = np.column_stack(np.where(img > 0))

    if len(coords) == 0:
        return img

    angle = cv2.minAreaRect(coords)[-1]

    if angle < -45:
        angle = -(90 + angle)
    else:
        angle = -angle

    (h, w) = img.shape[:2]
    center = (w // 2, h // 2)

    M = cv2.getRotationMatrix2D(center, angle, 1.0)

    return cv2.warpAffine(
        img,
        M,
        (w, h),
        flags=cv2.INTER_CUBIC,
        borderMode=cv2.BORDER_REPLICATE
    )


# -----------------------------------------------------------------------------------
# 🧠 3. OCR 실행 (숫자 특화)
# -----------------------------------------------------------------------------------
def run_ocr(processed_img):
    """
    EasyOCR 실행

    ✔ allowlist 적용 → 숫자만 인식
    ✔ 정답지 인식 정확도 크게 상승
    """

    return reader.readtext(
        processed_img,
        detail=0,
        allowlist='0123456789.'
    )


# -----------------------------------------------------------------------------------
# 🔍 4. OCR 결과 파싱
# -----------------------------------------------------------------------------------
def parse_answers(results):
    """
    OCR 결과 → (문제번호, 정답) 형태로 변환

    예:
    "1 3 2 5" → [(1,3), (2,5)]
    """

    full_text = " ".join(results)

    # 🔧 오인식 보정
    replace_map = {
        '①': '1', '②': '2', '③': '3', '④': '4', '⑤': '5',
        'O': '0', 'l': '1', 'I': '1'
    }

    for k, v in replace_map.items():
        full_text = full_text.replace(k, v)

    # 🔎 정규식 추출
    matches = re.findall(r'(\d{1,3})\s*[\.\s]*\s*([1-5])', full_text)

    parsed = []
    used = set()

    for q_num, ans in matches:
        num = int(q_num)

        # 중복 방지 + 범위 제한
        if num in used or not (1 <= num <= 100):
            continue

        parsed.append((num, ans))
        used.add(num)

    return parsed, full_text


# -----------------------------------------------------------------------------------
# 🧪 5. OCR 전체 파이프라인
# -----------------------------------------------------------------------------------
def process_ocr_image(file_bytes, debug=False):
    """
    OCR 전체 처리 흐름

    1. 이미지 디코딩
    2. 전처리
    3. 기울기 보정
    4. OCR
    5. 결과 파싱
    """

    # bytes → OpenCV 이미지
    img_array = np.asarray(bytearray(file_bytes), dtype=np.uint8)
    img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

    if img is None:
        raise ValueError("이미지 디코딩 실패")

    # 전처리
    processed = preprocess_for_ocr(img)

    # 기울기 보정
    processed = deskew(processed)

    # 디버깅 이미지 저장
    if debug:
        os.makedirs("debug", exist_ok=True)
        cv2.imwrite("debug/processed.png", processed)

    # OCR 실행
    results = run_ocr(processed)

    # 결과 파싱
    parsed, full_text = parse_answers(results)

    return parsed, full_text


# -----------------------------------------------------------------------------------
# 📡 6. API ViewSet
# -----------------------------------------------------------------------------------
class QuestionViewSet(viewsets.ModelViewSet):
    queryset = Question.objects.all().order_by('id')
    serializer_class = QuestionSerializer


# -----------------------------------------------------------------------------------
# 🖥 7. 문제 리스트 화면
# -----------------------------------------------------------------------------------
def question_list(request):
    categories = Category.objects.all()
    exams = Exam.objects.all()

    category_id = request.GET.get('category')
    exam_id = request.GET.get('exam')

    questions = Question.objects.all()

    # 필터링
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


# -----------------------------------------------------------------------------------
# ✏️ 8. 시험 문제 일괄 수정
# -----------------------------------------------------------------------------------
def exam_bulk_edit(request, exam_id):
    exam = get_object_or_404(Exam, id=exam_id)
    questions = exam.questions.all().order_by('number')

    if request.method == "POST":
        for question in questions:
            new_answer = request.POST.get(f'answer_{question.id}')
            new_number = request.POST.get(f'number_{question.id}')

            if new_answer is not None:
                question.answer = new_answer

            if new_number is not None:
                question.number = new_number

            question.save()

        return redirect('admin:quiz_exam_changelist')

    return render(request, 'admin/quiz/bulk_edit.html', {
        'exam': exam,
        'questions': questions,
    })


# -----------------------------------------------------------------------------------
# 🖼 9. 이미지 자동 매칭
# -----------------------------------------------------------------------------------
@staff_member_required
def admin_image_matcher(request):
    if request.method == "POST":
        image_dir = request.POST.get('image_dir')

        if not os.path.exists(image_dir):
            messages.error(request, f"폴더 없음: {image_dir}")
            return redirect('admin_manual')

        files = [f for f in os.listdir(image_dir) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]

        success_count = 0

        for file_name in files:
            try:
                parts = os.path.splitext(file_name)[0].split('_')

                exam_id = int(parts[0])
                q_num = int(parts[1])

                question = Question.objects.get(exam_id=exam_id, number=q_num)

                # 보기 이미지
                if len(parts) == 3:
                    idx = int(parts[2]) - 1
                    choices = question.choices.all().order_by('id')

                    if idx < len(choices):
                        with open(os.path.join(image_dir, file_name), 'rb') as f:
                            choices[idx].image_file.save(file_name, File(f), save=True)
                        success_count += 1

                # 문제 이미지
                else:
                    with open(os.path.join(image_dir, file_name), 'rb') as f:
                        img = QuestionImage(question=question)
                        img.image_file.save(file_name, File(f), save=True)
                    success_count += 1

            except Exception as e:
                print(f"[ERROR] {file_name}: {e}")
                continue

        messages.success(request, f"{success_count}개 이미지 매칭 완료")
        return redirect('admin_manual')

    return redirect('admin_manual')


# -----------------------------------------------------------------------------------
# ⚙️ 10. 관리자 페이지
# -----------------------------------------------------------------------------------
@staff_member_required
def admin_manual(request):
    exams = Exam.objects.all()
    return render(request, 'admin/quiz/admin_manual.html', {'exams': exams})


# -----------------------------------------------------------------------------------
# 🔥 11. OCR 정답 자동 입력 (최종 완성)
# -----------------------------------------------------------------------------------
@staff_member_required
def admin_answer_ocr_update(request):
    if request.method == "POST" and request.FILES.get('answer_sheet'):

        exam_id = request.POST.get('exam_id')
        exam = get_object_or_404(Exam, id=exam_id)
        image_file = request.FILES['answer_sheet']

        try:
            # 🔥 OCR 파이프라인 실행
            parsed, full_text = process_ocr_image(
                image_file.read(),
                debug=True  # debug 이미지 저장
            )

            print("\n[OCR TEXT]")
            print(full_text)

            update_count = 0

            for num, ans in parsed:
                question = Question.objects.filter(exam=exam, number=num).first()

                if question:
                    question.answer = ans
                    question.save()
                    update_count += 1

            if update_count > 0:
                messages.success(request, f"{update_count}개 정답 자동 입력 완료")
            else:
                messages.warning(request, "정답 인식 실패 (이미지 확인 필요)")

        except Exception as e:
            print("[OCR ERROR]", e)
            messages.error(request, f"OCR 처리 실패: {e}")

        return redirect('admin_manual')