import os
import json
import django
import tkinter as tk
from tkinter import filedialog, messagebox

# 1. Django 환경 설정 (내 프로젝트 폴더 이름 확인)
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "my_quiz_site.settings")
django.setup()

# Django 설정이 끝난 후에 모델을 불러와야 오류가 나지 않습니다.
from quiz.models import Question, Choice, Category, Exam

def select_json_file():
    """윈도우 파일 탐색기를 띄워 JSON 파일을 선택합니다."""
    root = tk.Tk()
    root.withdraw() # 메인 창은 숨김
    
    file_path = filedialog.askopenfilename(
        title="DB에 넣을 JSON 파일을 선택하세요",
        filetypes=[("JSON 파일", "*.json")]
    )
    return file_path

def insert_quiz_data(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        
        try:
            data = json.load(f)
        except json.JSONDecodeError:
            print("에러: JSON 파일 형식이 올바르지 않습니다.")
            return
    
    # 분류 입력 받기
    category_name = input("분류를 입력하세요 (예: 정보처리기사 필기): ").strip()
    if not category_name:
        print("에러: 분류는 필수 입력 사항입니다.")
        return
    
    # 제목 입력 받기 (디폴트: 파일명)
    default_title = os.path.splitext(os.path.basename(file_path))[0]
    exam_title = input(f"시험지 제목을 입력하세요 (기본값: {default_title}): ").strip()
    if not exam_title:
        exam_title = default_title   
    
    # --- DB 작업 시작 ---
    print(f"\n데이터 등록을 시작합니다: [{category_name} > {exam_title}]")
    
    # 분류 생성 또는 가져오기
    category, _ = Category.objects.get_or_create(name=category_name)
    
    # 시험지 생성
    exam = Exam.objects.create(category=category, title=exam_title)
    
    
    count = 0
    for item in data:
        content = item.get('content') or ''
        
        # [필터링 조건 추가]
        # 1. 내용에 '정답'이라는 단어가 있거나
        # 2. 내용이 10글자 미만으로 너무 짧은 경우 (예: "1. ①") 스킵!
        if "정답" in content or len(content) < 10:
            continue
        
        # 문제 생성 시 exam과 연결    
        question = Question.objects.create(
            exam=exam,
            number=item.get('no'),
            content=content,
            answer=item.get('answer')
        )
        
        # 보기(Choice)들을 반복문으로 생성하여 문제와 연결
        options = item.get('options', [])
        target_answer = str(item.get('answer', '')) # JSON의 정답 값
        
        for idx, option_text in enumerate(options, 1):
            # 정답 텍스트(예: "①")가 보기 텍스트에 포함되어 있거나, 
            # 인덱스가 정답 번호와 일치하는지 체크
        
            is_this_answer = False
            # 정답 기호(①)나 숫자(1)가 포함되어 있는지 체크
            if target_answer in option_text or str(idx) in target_answer:
                is_this_answer = True

            Choice.objects.create(
                question=question,
                choice_text=option_text,
                is_answer=is_this_answer # 여기서 자동으로 정답 설정!
            )            
        
        count += 1
    
    print(f"성공: {count}개의 실제 문제만 추가되었습니다.")        
 
    #except Exception as e:
    #    messagebox.showerror("오류", f"데이터 입력 중 오류: {e}")
        
if __name__ == "__main__":
    root = tk.Tk()
    root.withdraw()
    # 1. 파일 선택
    # selected_json = select_json_file()
    selected_json = filedialog.askopenfilename(filetypes=[("JSON 파일", "*.json")])
    
    if selected_json:
        # 2. DB 입력 실행
        insert_quiz_data(selected_json)
    else:
        print("파일 선택이 취소되었습니다.")