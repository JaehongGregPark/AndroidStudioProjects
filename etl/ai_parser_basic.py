import os
import json
import google.generativeai as genai
from pdf2image import convert_from_path
from PIL import Image

# 1. 설정
genai.configure(api_key="AIzaSyCSkZFW9LCUFvngTiI8GCGuaqgJIimKiUo")
model = genai.GenerativeModel('gemini-1.5-flash') # 속도가 빠르고 무료인 flash 모델 사용

def extract_with_ai(pdf_path):
    # PDF 한 페이지를 이미지로 변환
    pages = convert_from_path(pdf_path, 300)
    final_data = []

    for i, page in enumerate(pages):
        img_path = f"temp_p{i}.png"
        page.save(img_path, "PNG")

        prompt = """
        이 이미지는 정보처리기사 시험지야. 
        문제를 하나씩 분석해서 아래 JSON 형식의 배열로 응답해줘.
        표가 있다면 표의 내용을 텍스트나 마크다운으로 최대한 보존해서 content에 넣어줘.
        
        형식:
        [{
            "no": "문제번호",
            "content": "문제 지문 전체",
            "options": ["①내용", "②내용", "③내용", "④내용"],
            "answer": "이미지에 정답이 표시되어 있다면 포함, 없으면 null"
        }]
        
        주의: 반드시 순수한 JSON 배열만 응답하고 다른 설명은 하지 마.
        """

        # AI에게 전송
        img = Image.open(img_path)
        response = model.generate_content([prompt, img])
        
        try:
            # AI의 응답에서 JSON만 추출
            clean_json = response.text.replace('```json', '').replace('```', '').strip()
            page_data = json.loads(clean_json)
            final_data.extend(page_data)
            print(f"{i+1}페이지 추출 완료")
        except:
            print(f"{i+1}페이지 해석 실패")
        
        os.remove(img_path) # 임시파일 삭제

    return final_data

# 실행 및 저장
# result = extract_with_ai("20260404_test.pdf")
# with open("clean_result.json", "w", encoding="utf-8") as f:
#     json.dump(result, f, ensure_ascii=False, indent=4)