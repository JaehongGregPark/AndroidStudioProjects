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
    try:
        with open(json_file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        count = 0
        for item in data:
            q_num = int(item.get('no', 0))
            content = item.get('content', '').strip()

            # [핵심] 동일한 번호와 내용이 있으면 새로 만들지 않고 가져옵니다.
            question, created = Question.objects.get_or_create(
                number=q_num,
                content=content
            )

            if created: # 새로 생성된 경우에만 보기를 추가합니다.
                options = item.get('options', [])
                for opt_text in options:
                    Choice.objects.create(
                        question=question,
                        choice_text=opt_text.strip()
                    )
                count += 1

        messagebox.showinfo("완료", f"새로 추가된 문제: {count}개")
        
    except Exception as e:
        messagebox.showerror("오류", f"데이터 입력 중 오류: {e}")

if __name__ == "__main__":
    # 1. 파일 선택
    selected_json = select_json_file()
    
    if selected_json:
        # 2. DB 입력 실행
        insert_quiz_data(selected_json)
    else:
        print("파일 선택이 취소되었습니다.")