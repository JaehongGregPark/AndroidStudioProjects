import os
import json
import time
from google import genai
import tkinter as tk
from tkinter import filedialog, messagebox
import pypdf # 설치 필요: pip install pypdf

# 1. AI 설정
API_KEY = "AIzaSyBK4gzUuTaGViRWTf2osd2eq5E0eHT9Olo"
client = genai.Client(api_key=API_KEY)
MODEL_ID = "gemini-2.5-flash"

def get_ai_response(text_content):
    """이미지 대신 텍스트를 전달하여 토큰 소모를 최소화합니다."""
    prompt = """
    아래는 정보처리기사 시험지 텍스트야. 
    문제를 분석해서 반드시 JSON 배열 형식으로만 응답해줘.
    표가 포함된 문제는 표의 내용을 텍스트로 잘 정리해서 content에 넣어줘.
    
    형식: [{"no": "번호", "content": "지문", "options": ["①..","②..","③..","④.."], "answer": null}]
    """
    
    try:
        response = client.models.generate_content(
            model=MODEL_ID,
            contents=[prompt, text_content]
        )
        if response and response.text:
            clean_json = response.text.replace('```json', '').replace('```', '').strip()
            return json.loads(clean_json)
        return []
    except Exception as e:
        print(f"   에러 발생: {e}")
        return []

def process_pdf_text(file_path):
    """PDF에서 텍스트를 먼저 추출한 뒤 AI에게 보냅니다."""
    from pypdf import PdfReader
    
    print(f"[{os.path.basename(file_path)}] 텍스트 추출 중...")
    reader = PdfReader(file_path)
    all_data = []

    for i, page in enumerate(reader.pages):
        print(f"> {i+1} / {len(reader.pages)} 페이지 분석 중...")
        text = page.extract_text()
        
        if text.strip():
            result = get_ai_response(text)
            if result:
                all_data.extend(result)
        
        # 무료 티어 안전 장치 (10초 대기)
        time.sleep(10)
        
    return all_data

if __name__ == "__main__":
    # 파일 선택 로직 (기존과 동일)
    root = tk.Tk()
    root.withdraw()
    path = filedialog.askopenfilename(filetypes=[("PDF 파일", "*.pdf")])
    
    if path:
        final_results = process_pdf_text(path)
        if final_results:
            output_name = f"final_data.json"
            with open(output_name, "w", encoding="utf-8") as f:
                json.dump(final_results, f, ensure_ascii=False, indent=4)
            print(f"성공! {len(final_results)}개의 문제가 저장되었습니다.")