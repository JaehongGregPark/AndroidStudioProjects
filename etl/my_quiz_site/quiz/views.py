# my_quiz_site/quiz/views.py

from django.contrib.admin.views.decorators import staff_member_required
from django.contrib import messages
from django.shortcuts import render, redirect, get_object_or_404
from rest_framework import viewsets
from django.core.files import File

from .models import Exam, Question, QuestionImage, Choice, Category
from .serializers import QuestionSerializer

import os
import io
import cv2
import numpy as np
import easyocr
import re
import fitz  # PyMuPDF
from google.cloud import vision


# OCR 모델
reader = easyocr.Reader(['en'], gpu=False)


# ---------------------------------------------------------
# preprocess
# ---------------------------------------------------------
def preprocess(img):

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # threshold 제거
    gray = cv2.resize(gray, None, fx=2, fy=2)

    return gray


# ---------------------------------------------------------
# 컬럼 분할 (2컬럼)
# ---------------------------------------------------------
def split_columns(img):

    h,w = img.shape

    mid = w // 2

    left = img[:, :mid]
    right = img[:, mid:]

    return [left, right]


# ---------------------------------------------------------
# row 분할
# ---------------------------------------------------------
def split_rows(img):

    projection = np.mean(img, axis=1)

    threshold = np.mean(projection) * 0.9

    rows = []
    start = None

    for i, val in enumerate(projection):

        if val < threshold and start is None:
            start = i

        elif val >= threshold and start is not None:

            if i - start > 8:
                rows.append(img[start:i])

            start = None

    return rows


# ---------------------------------------------------------
# 정답 OCR
# ---------------------------------------------------------
def read_answer(row):

    h, w = row.shape

    crop = row[:, int(w*0.45):]

    crop = cv2.resize(
        crop,
        None,
        fx=3,
        fy=3,
        interpolation=cv2.INTER_CUBIC
    )

    results = reader.readtext(
        crop,
        detail=0
    )

    if len(results) == 0:
        return None

    text = results[0]

    # 유니코드 숫자 매핑
    mapping = {
        "①": "1",
        "②": "2",
        "③": "3",
        "④": "4"
    }

    for k,v in mapping.items():
        if k in text:
            return v

    # fallback 일반 숫자
    for ch in text:
        if ch in "1234":
            return ch

    return None

# ---------------------------------------------------------
# OCR main
# ---------------------------------------------------------
def process_ocr(file_bytes, debug=False):

    img_array = np.asarray(bytearray(file_bytes), dtype=np.uint8)
    img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    H, W = gray.shape
    right = gray[:, int(W * 0.6):]

    _, th = cv2.threshold(
        right,
        0,
        255,
        cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU
    )

    contours, _ = cv2.findContours(
        th,
        cv2.RETR_EXTERNAL,
        cv2.CHAIN_APPROX_SIMPLE
    )

    boxes = []

    for cnt in contours:

        x, y, w, h = cv2.boundingRect(cnt)

        if h < 15:
            continue

        boxes.append((x, y, w, h))

    boxes.sort(key=lambda b: b[1])

    rights = []

    for x, y, w, h in boxes:

        pad = 10

        x1 = max(0, x - pad)
        y1 = max(0, y - pad)
        x2 = min(right.shape[1], x + w + pad)
        y2 = min(right.shape[0], y + h + pad)

        roi = right[y1:y2, x1:x2]

        roi = cv2.resize(roi, None, fx=3, fy=3)

        _, th = cv2.threshold(
            roi,
            0,
            255,
            cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU
        )

        # 원 제거
        kernel = np.ones((3,3), np.uint8)
        th = cv2.erode(th, kernel, iterations=2)

        ys, xs = np.where(th > 0)

        if len(xs) == 0:
            continue

        rightmost = x1 + np.max(xs)

        rights.append((y, rightmost))

    xs = [r[1] for r in rights]

    clusters = np.percentile(xs, [25, 50, 75])

    results = []

    for i, (_, rpos) in enumerate(rights):

        if rpos < clusters[0]:
            ans = "1"
        elif rpos < clusters[1]:
            ans = "2"
        elif rpos < clusters[2]:
            ans = "3"
        else:
            ans = "4"

        results.append((i + 1, ans))

    return results



def create_templates():

    templates = {}

    for num in ["1","2","3","4"]:

        img = np.zeros((60,60), dtype=np.uint8)

        cv2.putText(
            img,
            num,
            (10,45),
            cv2.FONT_HERSHEY_SIMPLEX,
            1.5,
            255,
            3
        )

        templates[num] = img

    return templates


def template_match(gray, templates):

    matches = []

    for num, tmpl in templates.items():

        res = cv2.matchTemplate(
            gray,
            tmpl,
            cv2.TM_CCOEFF_NORMED
        )

        loc = np.where(res > 0.4)

        for pt in zip(*loc[::-1]):
            x,y = pt
            matches.append((y, num))

    return matches

# =========================================================
# API
# =========================================================
class QuestionViewSet(viewsets.ModelViewSet):
    queryset = Question.objects.all().order_by('id')
    serializer_class = QuestionSerializer


# =========================================================
# question list
# =========================================================
def question_list(request):

    categories = Category.objects.all()
    exams = Exam.objects.all()

    category_id = request.GET.get('category')
    exam_id = request.GET.get('exam')

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


# =========================================================
# bulk edit
# =========================================================
def exam_bulk_edit(request, exam_id):

    exam = get_object_or_404(Exam, id=exam_id)
    questions = exam.questions.all().order_by('number')

    if request.method == "POST":

        for question in questions:
            question.answer = request.POST.get(f'answer_{question.id}')
            question.number = request.POST.get(f'number_{question.id}')
            question.save()

        return redirect('admin:quiz_exam_changelist')

    return render(request, 'admin/quiz/bulk_edit.html', {
        'exam': exam,
        'questions': questions,
    })


# 1. 구글 인증 키 설정 (본인의 JSON 키 파일 경로로 수정하세요)
# 예: os.path.join(settings.BASE_DIR, 'keys', 'your-google-key.json')
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "C:/Users/USER/Documents/project/etl/my_quiz_site/keys/service-account-key.json"

@staff_member_required
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
        #matches = re.findall(r'(\d{1,3})\s*[\.\s]*\s*([1-5])', processed_text)
        
        # 마침표가 없어도 숫자들 사이의 공백이나 경계를 더 잘 잡도록 보강
        matches = re.findall(r'(\d{1,3})[\s\.]+(\d)', processed_text)
        
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

# =========================================================
# OCR 입력
# =========================================================
@staff_member_required
def admin_answer_ocr_update_old(request):

    if request.method == "POST" and request.FILES.get('answer_sheet'):

        exam = get_object_or_404(
            Exam,
            id=request.POST.get('exam_id')
        )

        parsed = process_ocr(
            request.FILES['answer_sheet'].read(),
            debug=True
        )

        count = 0

        for num, ans in parsed:

            q = Question.objects.filter(
                exam=exam,
                number=num
            ).first()

            if q:
                q.answer = ans
                q.save()
                count += 1

        messages.success(request, f"{count}개 자동 입력 완료")

        return redirect('admin_manual')

@staff_member_required
def admin_image_matcher(request):

    if request.method == "POST":

        image_dir = request.POST.get('image_dir')

        if not os.path.exists(image_dir):
            messages.error(request, "폴더를 찾을 수 없습니다.")
            return redirect('admin_manual')

        success_count = 0

        for file_name in os.listdir(image_dir):

            try:
                name_part = os.path.splitext(file_name)[0]
                parts = name_part.split('_')

                exam_id = int(parts[0])
                q_num = int(parts[1])

                question = Question.objects.get(
                    exam_id=exam_id,
                    number=q_num
                )

                file_path = os.path.join(image_dir, file_name)

                # 보기 이미지
                if len(parts) == 3:

                    idx = int(parts[2]) - 1
                    choices = question.choices.all().order_by('id')

                    if idx < len(choices):
                        with open(file_path, 'rb') as f:
                            choices[idx].image_file.save(
                                file_name,
                                File(f),
                                save=True
                            )

                # 문제 이미지
                else:

                    with open(file_path, 'rb') as f:
                        img = QuestionImage(question=question)
                        img.image_file.save(
                            file_name,
                            File(f),
                            save=True
                        )

                success_count += 1

            except Exception as e:
                print("IMAGE MATCH ERROR:", e)

        messages.success(
            request,
            f"{success_count}개 이미지 매칭 완료"
        )

        return redirect('admin_manual')

    return redirect('admin_manual')

# =========================================================
# admin page
# =========================================================
@staff_member_required
def admin_manual(request):
    if request.method == "POST" and request.FILES.get('upload_file'):
        exam_id = request.POST.get('exam_id')
        new_exam_title = request.POST.get('new_exam_title')
        mode = request.POST.get('upload_mode')
        uploaded_file = request.FILES['upload_file']

        # 시험지 확보 (없으면 생성)
        exam = None
        if exam_id:
            exam = get_object_or_404(Exam, id=exam_id)
        elif new_exam_title:
            category, _ = Category.objects.get_or_create(name="정보처리기사 실기")
            exam = Exam.objects.create(category=category, title=new_exam_title)

        if mode == 'practical':
            try:
                # 1. PDF를 이미지로 변환하여 OCR 실행
                client = vision.ImageAnnotatorClient()
                doc = fitz.open(stream=uploaded_file.read(), filetype="pdf")
                full_text = ""

                for page in doc:
                    # 페이지를 고해상도 이미지로 렌더링
                    pix = page.get_pixmap(matrix=fitz.Matrix(2, 2)) 
                    img_bytes = pix.tobytes("jpg")
                    
                    # Google OCR 호출
                    image = vision.Image(content=img_bytes)
                    response = client.text_detection(image=image)
                    full_text += response.text_annotations[0].description + "\n"

                # 2. 추출된 텍스트 기반 실기 파싱 (이전 로직과 동일)
                # 태그나 숫자. 과목명 패턴 탐색
                subject_ptrn = r'(\d\.\s*(?:알고리즘|데이터베이스|업무프로세스|신기술동향|전산영어))'
                parts = re.split(subject_ptrn, full_text)
                answer_text = full_text.split("정답")[-1] if "정답" in full_text else ""
                
                created_count = 0
                if len(parts) > 1:
                    for i in range(1, len(parts), 2):
                        subject_header = parts[i]
                        content_body = parts[i+1]
                        
                        # 문항 기호 추출 및 등록
                        markers = re.findall(r'([①-⑮]|\(\d\))', content_body)
                        for marker in sorted(list(set(markers))):
                            ans_match = re.search(rf'{re.escape(marker)}\s*([^\n①-⑮\(]+)', answer_text)
                            Question.objects.create(
                                exam=exam,
                                number=99,
                                content=f"{subject_header}\n{content_body.strip()}",
                                answer=ans_match.group(1).strip() if ans_match else "미등록"
                            )
                            created_count += 1
                
                messages.success(request, f"OCR 분석 완료: {created_count}개 문항 등록 성공!")
            except Exception as e:
                messages.error(request, f"OCR 처리 중 오류: {str(e)}")

        return redirect('admin_manual')

    exams = Exam.objects.all().order_by('-created_at')
    return render(request, 'admin/quiz/admin_manual.html', {'exams': exams})

 
def parse_practical_logic(exam, text):
    """실기 텍스트 분석 핵심 로직 (보조 함수)"""
    # 과목 구분자 패턴 (2009년 실기 자료 근거) [cite: 37, 38, 62, 81, 92]
    subject_ptrn = r'(\d\.\s*(?:알고리즘|데이터베이스|업무프로세스|신기술동향|전산영어))'
    parts = re.split(subject_ptrn, text)
    answer_text = text.split("정답>")[-1] if "정답>" in text else ""
    
    count = 0
    for i in range(1, len(parts), 2):
        subject_name = parts[i]
        content = parts[i+1]
        # 문항 기호 추출 (①-⑮, (1)-(5)) [cite: 5, 57, 107]
        markers = re.findall(r'([①-⑮]|\(\d\))', content)
        for marker in sorted(list(set(markers))):
            # 정답지 영역에서 해당 마커 정답 탐색 [cite: 110]
            ans_match = re.search(rf'{re.escape(marker)}\s*(\d+\.\s*[^\n]+|[A-Za-z\s]+)', answer_text)
            Question.objects.create(
                exam=exam,
                number=99, # 임시 번호 (관리자 화면에서 수정 가능)
                content=content.strip(),
                answer=ans_match.group(1).strip() if ans_match else "미확인"
            )
            count += 1
    return count


def quiz_detail(request, exam_id):
    exam = get_object_or_404(Exam, id=exam_id)
    # 번호 순서대로 문제 정렬
    questions = exam.questions.all().order_by('number')
    
    return render(request, 'quiz/quiz_detail.html', {
        'exam': exam,
        'questions': questions
    })
    
def admin_bulk_practical_upload(request):
    if request.method == "POST":
        exam_id = request.POST.get('exam_id')
        exam = Exam.objects.get(id=exam_id)
        raw_text = request.POST.get('bulk_text')

        # 1. 과목 섹션 분리 (번호 + 과목명 패턴)
        # 예: "2. 데이터베이스 [배점: 30점]" 패턴을 기준으로 나눔
        subject_patterns = r'(\d\.\s*(?:알고리즘|데이터베이스|업무프로세스|신기술동향|전산영어).*?)(?=\d\.\s*(?:알고리즘|데이터베이스|업무프로세스|신기술동향|전산영어)|$)'
        sections = re.findall(subject_patterns, raw_text, re.DOTALL)

        # 2. 정답지 추출 (6페이지의 정답 섹션 찾기)
        answer_section = raw_text.split("정답>")[-1] if "정답>" in raw_text else ""
        
        created_count = 0
        for section_content in sections:
            # 과목명 추출
            subject_name = re.search(r'(알고리즘|데이터베이스|업무프로세스|신기술동향|전산영어)', section_content).group(1)
            
            # 해당 섹션 내의 문항 번호 추출 (①, ②... 또는 (1), (2)...)
            # 실기 시험 특성상 한 지문에 여러 문항이 딸려 있음
            q_markers = re.findall(r'([①-⑮]|\(\d\))', section_content)
            unique_markers = sorted(list(set(q_markers)))

            for marker in unique_markers:
                # 번호 표준화 (① -> 1, (1) -> 1)
                clean_num = marker.replace('(', '').replace(')', '')
                # 특수문자 대응 테이블 (필요시 확장)
                marker_map = {'①':1,'②':2,'③':3,'④':4,'⑤':5,'⑥':6,'⑦':7,'⑧':8,'⑨':9,'⑩':10}
                num = marker_map.get(clean_num, clean_num)

                # 정답 매칭 (정답지 텍스트에서 해당 과목의 번호를 찾음)
                # 정교한 매칭 로직은 정답지 텍스트 구조에 따라 보정이 필요함
                ans_pattern = rf'{marker}\s*(\d+\.\s*[^\n]+|[A-Za-z\s]+)'
                ans_match = re.search(ans_pattern, answer_section)
                ans_text = ans_match.group(1).strip() if ans_match else ""

                Question.objects.create(
                    exam=exam,
                    number=num, # 실제로는 과목별 구분이 필요하므로 고유값 생성 로직 필요
                    content=section_content, # 해당 과목 지문 전체를 저장
                    answer=ans_text,
                    explanation=f"{subject_name} 과목의 {marker} 문항입니다."
                )
                created_count += 1

        messages.success(request, f"{created_count}개의 실기 문항이 자동으로 분류 및 등록되었습니다.")
        return redirect('admin_manual')

    exams = Exam.objects.all().order_by('-created_at')
    return render(request, 'quiz/admin_bulk_practical.html', {'exams': exams})   

def admin_bulk_file_upload(request):
    if request.method == "POST" and request.FILES.get('quiz_file'):
        exam_id = request.POST.get('exam_id')
        exam = Exam.objects.get(id=exam_id)
        pdf_file = request.FILES['quiz_file']

        # 1. PDF에서 텍스트 추출
        doc = fitz.open(stream=pdf_file.read(), filetype="pdf")
        full_text = ""
        for page in doc:
            full_text += page.get_text()

        # 2. 과목 섹션 분리 (알고리즘, DB, 업무프로세스 등)
        # 제시된 PDF 구조상 '숫자. 과목명' 패턴을 기준으로 분할 [cite: 38, 62, 81, 92]
        subject_split_ptrn = r'(\d\.\s*(?:알고리즘|데이터베이스|업무프로세스|신기술동향|전산영어))'
        parts = re.split(subject_split_ptrn, full_text)
        
        # 3. 정답지 추출 (마지막 페이지의 정답 섹션 활용) [cite: 110]
        answer_text = full_text.split("정답>")[-1] if "정답>" in full_text else ""

        created_count = 0
        # split 결과는 [공백, 과목명1, 내용1, 과목명2, 내용2...] 순서임
        for i in range(1, len(parts), 2):
            subject_header = parts[i]
            section_content = parts[i+1]
            subject_name = re.search(r'(알고리즘|데이터베이스|업무프로세스|신기술동향|전산영어)', subject_header).group(1)

            # 문항 번호 추출 (①, ②... 또는 (1), (2)...) [cite: 39, 40]
            q_markers = re.findall(r'([①-⑮]|\(\d\))', section_content)
            unique_markers = sorted(list(set(q_markers)))

            for marker in unique_markers:
                # 정답 매칭 로직 (정답지에서 마커에 해당하는 텍스트 탐색) [cite: 124, 125]
                ans_pattern = rf'{re.escape(marker)}\s*(\d+\.\s*[^\n]+|[A-Za-z\s]+)'
                ans_match = re.search(ans_pattern, answer_text)
                ans_val = ans_match.group(1).strip() if ans_match else ""

                Question.objects.create(
                    exam=exam,
                    number=99,  # 실기는 과목내 순번이 중요하므로 추후 정렬 로직 보강 필요
                    content=section_content.strip(),
                    answer=ans_val,
                    explanation=f"[{subject_name}] {marker} 문항 정답입니다."
                )
                created_count += 1

        messages.success(request, f"파일 분석 완료: {created_count}개의 문항이 등록되었습니다.")
        return redirect('admin_manual')

    exams = Exam.objects.all().order_by('-created_at')
    return render(request, 'quiz/admin_file_upload.html', {'exams': exams}) 