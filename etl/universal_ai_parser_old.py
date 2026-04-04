import os
import json
import google.generativeai as genai
from pdf2image import convert_from_path
from docx import Document # 워드 파일용
from PIL import Image
import tkinter as tk
from tkinter import filedialog, messagebox

# ==========================================
# 1. AI 설정
# ==========================================
API_KEY = "AIzaSyCSkZFW9LCUFvngTiI8GCGuaqgJIimKiUo"
genai.configure(api_key=API_KEY)
model = genai.GenerativeModel('gemini-1.5-flash')

def select_any_file():
    """다양한 형식의 파일을 선택합니다."""
    root = tk.Tk()
    root.withdraw()
    file_path = filedialog.askopenfilename(
        title="분석할 파일을 선택하세요 (PDF, Word, TXT)",
        filetypes=[
            ("모든 문서", "*.pdf *.docx *.txt"),
            ("PDF 파일", "*.pdf"),
            ("Word 파일", "*.docx"),
            ("텍스트 파일", "*.txt")
        ]
    )
    return file_path

def get_ai_response(content):
    """문자열 또는 이미지 리스트를 받아 AI로부터 JSON을 추출합니다."""
    prompt = """
    너는 전문 시험문제 분석가야. 제공된 내용을 분석해서 JSON 배열로 응답해줘.
    형식: [{"no": "번호", "content": "지문", "options": ["①..","②..","③..","④.."], "answer": null}]
    반드시 순수한 JSON 배열만 응답해.
    """
    # content가 리스트(이미지 포함)면 이미지 분석, 문자열이면 텍스트 분석 실행
    response = model.generate_content([prompt] + (content if isinstance(content, list) else [content]))
    
    try:
        clean_json = response.text.replace('```json', '').replace('```', '').strip()
        return json.loads(clean_json)
    except:
        print("JSON 변환 실패")
        return []

def process_file(file_path):
    """확장자에 따라 적절한 추출 방식을 선택합니다."""
    ext = os.path.splitext(file_path)[1].lower()
    print(f"[{os.path.basename(file_path)}] 분석 시작...")

    # [Case 1] PDF 파일: 이미지 변환 후 AI 분석 (표/그림 보존)
    if ext == '.pdf':
        pages = convert_from_path(file_path, 300)
        all_data = []
        for i, page in enumerate(pages):
            temp_img = f"temp_p{i}.png"
            page.save(temp_img, "PNG")
            img = Image.open(temp_img)
            all_data.extend(get_ai_response([img]))
            os.remove(temp_img)
        return all_data

    # [Case 2] Word 파일 (.docx)
    elif ext == '.docx':
        doc = Document(file_path)
        full_text = "\n".join([para.text for para in doc.paragraphs])
        return get_ai_response(full_text)

    # [Case 3] 텍스트 파일 (.txt)
    elif ext == '.txt':
        with open(file_path, 'r', encoding='utf-8') as f:
            full_text = f.read()
        return get_ai_response(full_text)

    return None

# ==========================================
# 3. 메인 실행
# ==========================================
if __name__ == "__main__":
    file_path = select_any_file()
    
    if file_path:
        final_results = process_file(file_path)
        
        if final_results:
            output_name = f"final_data_{os.path.basename(file_path)}.json"
            with open(output_name, "w", encoding="utf-8") as f:
                json.dump(final_results, f, ensure_ascii=False, indent=4)
            
            messagebox.showinfo("성of공", f"분석 완료! {len(final_results)}개 추출됨\n파일: {output_name}")
    else:
        print("취소되었습니다.")