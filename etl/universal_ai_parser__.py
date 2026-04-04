import os
import json
# 새로운 라이브러리로 변경
from google import genai
from google.genai import types
from pdf2image import convert_from_path
from docx import Document
from PIL import Image
import tkinter as tk
from tkinter import filedialog, messagebox
import time

# ==========================================
# 1. AI 설정 (최신 SDK 방식)
# ==========================================
API_KEY = "AIzaSyCSkZFW9LCUFvngTiI8GCGuaqgJIimKiUo"
client = genai.Client(api_key=API_KEY)
#client = genai.Client(api_key=API_KEY, http_options={'api_version': 'v1'})
MODEL_ID = "gemini-2.0-flash" # 모델명 수정

def select_any_file():
    root = tk.Tk()
    root.withdraw()
    return filedialog.askopenfilename(
        title="분석할 파일을 선택하세요",
        filetypes=[("모든 문서", "*.pdf *.docx *.txt")]
    )

def get_ai_response(content_list):
    """에러 발생 시 잠시 대기 후 최대 3번까지 재시도합니다."""
    prompt = """
    너는 전문 시험문제 분석가야. 제공된 내용을 분석해서 JSON 배열로 응답해줘.
    형식: [{"no": "번호", "content": "지문", "options": ["①..","②..","③..","④.."], "answer": null}]
    반드시 다른 설명 없이 순수한 JSON 배열만 응답해.
    """
    
    max_retries = 3  # 최대 재시도 횟수
    retry_delay = 10 # 에러 시 대기 시간 (초)

    for attempt in range(max_retries):
        try:
            response = client.models.generate_content(
                model='gemini-2.0-flash', 
                contents=[prompt] + content_list
            )
            
            if response and response.text:
                text = response.text
                clean_json = text.replace('```json', '').replace('```', '').strip()
                return json.loads(clean_json)
            return []

        except Exception as e:
            if "429" in str(e): # 속도 제한 에러일 경우
                print(f"   (속도 제한 감지! {retry_delay}초 후 다시 시도합니다... {attempt+1}/{max_retries})")
                time.sleep(retry_delay)
                retry_delay += 10 # 다음 시도 때는 더 오래 대기
            else:
                print(f"상세 에러 내용: {e}")
                break # 다른 종류의 에러면 중단
                
    return []
            
def process_file(file_path):
    ext = os.path.splitext(file_path)[1].lower()
    print(f"[{os.path.basename(file_path)}] 분석 시작...")

    if ext == '.pdf':
        poppler_path = r'C:\poppler\Library\bin' 
        # DPI를 150으로 낮추면 데이터량이 줄어 429 에러가 훨씬 덜 납니다.
        pages = convert_from_path(file_path, 150, poppler_path=poppler_path)
        
        all_data = []
        for i, page in enumerate(pages):
            print(f"> {i+1} / {len(pages)} 페이지 처리 중...")
            
            result = get_ai_response([page])
            if result:
                all_data.extend(result)
            
            # 페이지 사이 기본 대기 시간 6초 (무료 티어 권장 사항)
            if i < len(pages) - 1:
                time.sleep(6)
                
        return all_data
    
    elif ext == '.docx':
        doc = Document(file_path)
        full_text = "\n".join([para.text for para in doc.paragraphs])
        return get_ai_response([full_text])

    elif ext == '.txt':
        with open(file_path, 'r', encoding='utf-8') as f:
            return get_ai_response([f.read()])

    return None

if __name__ == "__main__":
    file_path = select_any_file()
    if file_path:
        final_results = process_file(file_path)
        if final_results:
            output_name = f"final_data_{os.path.basename(file_path)}.json"
            with open(output_name, "w", encoding="utf-8") as f:
                json.dump(final_results, f, ensure_ascii=False, indent=4)
            messagebox.showinfo("성공", f"분석 완료! {len(final_results)}개 추출됨")