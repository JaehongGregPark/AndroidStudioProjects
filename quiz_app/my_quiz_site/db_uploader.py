import os
import json
import django

# 1. Django 환경 설정 (내 프로젝트 이름을 가져옵니다)
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "my_quiz_site.settings")
django.setup()

from quiz.models import Question, Choice

def insert_quiz_data(json_file_path):
    # 2. JSON 파일 읽기
    with open(json_file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    print(f"{len(data)}개의 데이터를 DB에 넣기 시작합니다...")

    for item in data:
        # 3. Question(문제) 저장
        q = Question.objects.create(
            number=int(item['no']),
            content=item['content']
        )

        # 4. Choice(보기) 저장
        # 객관식 보기가 있는 경우에만 실행합니다.
        for opt in item.get('options', []):
            Choice.objects.create(
                question=q,
                choice_text=opt
            )
        
    print("모든 데이터가 성공적으로 저장되었습니다!")

if __name__ == "__main__":
    # 아까 생성된 JSON 파일 이름을 여기에 적으세요!
    target_json = "result_파일명.json" 
    insert_quiz_data(target_json)