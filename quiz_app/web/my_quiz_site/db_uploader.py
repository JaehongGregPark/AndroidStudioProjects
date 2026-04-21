import os
import json
import django
import tkinter as tk
from tkinter import filedialog, messagebox

# 1. Django 환경 설정 (내 프로젝트 폴더 이름 확인)
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "my_quiz_site.settings")
django.setup()

# Django 설정이 끝난 후에 모델을 불러와야 오류가 나지 않습니다.
from quiz.models import Question, Choice

def select_json_file():
    """윈도우 파일 탐색기를 띄워 JSON 파일을 선택합니다."""
    root = tk.Tk()
    root.withdraw() # 메인 창은 숨김
    
    file_path = filedialog.askopenfilename(
        title="DB에 넣을 JSON 파일을 선택하세요",
        filetypes=[("JSON 파일", "*.json")]
    )
    return file_path

def insert_quiz_data(json_file_path):
    """선택된 JSON 파일을 읽어 DB에 저장하는 함수"""
    try:
        with open(json_file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        count = 0
        print(f"\n[{os.path.basename(json_file_path)}] 데이터 입력 시작...")

        for item in data:
            # Question(문제) 생성
            # 번호가 비어있거나 이상할 경우를 대비해 예외처리
            try:
                q_num = int(item.get('no', 0))
            except:
                q_num = 0

            q = Question.objects.create(
                number=q_num,
                content=item.get('content', '')
            )

            # Choice(보기) 생성
            options = item.get('options', [])
            for opt_text in options:
                Choice.objects.create(
                    question=q,
                    choice_text=opt_text
                )
            
            count += 1
            if count % 10 == 0:
                print(f"{count}개 완료...")

        messagebox.showinfo("성공", f"총 {count}개의 문제가 DB에 저장되었습니다!")
        
    except Exception as e:
        messagebox.showerror("오류", f"데이터 입력 중 문제가 발생했습니다:\n{e}")

if __name__ == "__main__":
    # 1. 파일 선택
    selected_json = select_json_file()
    
    if selected_json:
        # 2. DB 입력 실행
        insert_quiz_data(selected_json)
    else:
        print("파일 선택이 취소되었습니다.")