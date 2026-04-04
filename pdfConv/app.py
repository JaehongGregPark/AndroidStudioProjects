import io
import fitz  # PyMuPDF
import pytesseract
from PIL import Image
from flask import Flask, render_template, request, send_file

# Tesseract 설치 경로 설정 (윈도우의 경우 필수)
# 예: r'C:\Program Files\Tesseract-OCR\tesseract.exe'
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

app = Flask(__name__)

def extract_text_from_pdf(pdf_file):
    # PDF 열기
    doc = fitz.open(stream=pdf_file.read(), filetype="pdf")
    full_text = ""

    for page_index in range(len(doc)):
        page = doc[page_index]
        
        # 1. 먼저 일반적인 텍스트 추출 시도
        text = page.get_text()
        
        # 2. 만약 텍스트가 너무 적으면 이미지로 간주하고 OCR 실행
        if len(text.strip()) < 10:
            # 페이지를 이미지로 변환 (DPI를 높여야 인식률이 좋음)
            pix = page.get_pixmap(matrix=fitz.Matrix(2, 2))
            img = Image.frombytes("RGB", [pix.width, pix.height], pix.samples)
            
            # OCR로 한국어와 영어 추출
            text = pytesseract.image_to_string(img, lang='kor+eng')
        
        full_text += f"--- Page {page_index + 1} ---\n"
        full_text += text + "\n\n"
        
    return full_text

# ... 나머지 @app.route 코드는 이전과 동일 ...

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/convert', methods=['POST'])
def convert():
    if 'file' not in request.files:
        return "파일이 없습니다.", 400
    
    file = request.files['file']
    if file.filename == '':
        return "선택된 파일이 없습니다.", 400

    if file and file.filename.endswith('.pdf'):
        # 텍스트 추출
        extracted_text = extract_text_from_pdf(file)
        
        # 메모리 상에 텍스트 파일 생성
        proxy = io.BytesIO()
        proxy.write(extracted_text.encode('utf-8'))
        proxy.seek(0)
        
        output_filename = file.filename.rsplit('.', 1)[0] + ".txt"
        
        return send_file(
            proxy,
            as_attachment=True,
            download_name=output_filename,
            mimetype='text/plain'
        )
    
    return "PDF 파일만 업로드 가능합니다.", 400

if __name__ == '__main__':
    app.run(debug=True)