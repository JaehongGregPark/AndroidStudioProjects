import os
import json
import google.generativeai as genai
from pdf2image import convert_from_path
from PIL import Image

# ==========================================
# 1. 설정 영역 (여기에 본인의 API 키를 넣으세요)
# ==========================================
API_KEY = "YOUR_GEMINI_API_KEY_HERE" # 구글 AI 스튜디오에서 발급받은 키
genai.configure(api_key=API_KEY)

# 사용할 AI 모델 설정 (이미지 분석이 가능한 pro 모델)
model = genai.GenerativeModel('gemini-1.5-pro')

def start_parsing(pdf_file_path):
    """
    PDF 파일을 읽어서 문제 데이터를 추출하는 메인 함수
    """
    
    # PDF 파일이 실제로 있는지 확인합니다.
    if not os.path.exists(pdf_file_path):
        print(f"오류: {pdf_file_path} 파일을 찾을 수 없습니다.")
        return

    print("1. PDF를 이미지로 변환하는 중입니다... (잠시만 기다려주세요)")
    # PDF의 각 페이지를 이미지 리스트로 변환합니다 (300DPI는 고화질 설정입니다)
    pages = convert_from_path(pdf_file_path, 300)
    
    total_data = [] # 모든 페이지의 문제를 담을 바구니

    print(f"2. 총 {len(pages)}페이지 분석을 시작합니다.")

    # 각 페이지를 하나씩 꺼내서 AI에게 물어봅니다.
    for i, page in enumerate(pages):
        print(f"--- {i+1}페이지 분석 중 ---")
        
        # AI에게 전달하기 위해 현재 페이지를 임시 이미지 파일로 저장합니다.
        temp_img_name = f"temp_page_{i+1}.png"
        page.save(temp_img_name, "PNG")

        # AI에게 시킬 "명령어(프롬프트)"입니다. 
        # 최대한 상세하게 적어야 정확한 결과를 줍니다.
        instruction = """
        너는 정보처리기사 시험지 전문 파서야. 
        이미지를 보고 다음 규칙에 맞춰 JSON 형식으로만 응답해줘.
        
        1. 분류(type): 필기 문제는 'PILGI', 실기 문제는 'SILGI'
        2. 문제번호(no): 숫자만 추출
        3. 지문(question): 문제 내용 전체 (코드가 있다면 ```로 감싸줘)
        4. 보기(options): 객관식일 때만 1,2,3,4번 내용을 리스트로, 주관식은 []
        5. 정답(answer): 정확한 정답 내용
        6. 이미지여부(has_img): 도면, 표, 그림이 포함되어 있으면 true, 아니면 false
        
        결과 예시: [{"no": 1, "type": "SILGI", "question": "...", "options": [], "answer": "..."}]
        """

        # 이미지 파일을 읽어서 AI에게 보낼 준비를 합니다.
        img_data = Image.open(temp_img_name)

        # AI에게 이미지와 명령어를 보내고 답변을 받습니다.
        response = model.generate_content([instruction, img_data])
        
        # AI의 답변(텍스트)에서 JSON 데이터만 깨끗하게 걸러냅니다.
        raw_text = response.text.replace('```json', '').replace('```', '').strip()
        
        try:
            # 글자 형태로 된 답변을 파이썬이 다룰 수 있는 '데이터(리스트/딕셔너리)'로 변환합니다.
            page_data = json.loads(raw_text)
            total_data.extend(page_data)
        except Exception as e:
            print(f"{i+1}페이지 해석 중 오류 발생: {e}")

        # 분석이 끝난 임시 이미지는 삭제합니다 (컴퓨터 정리)
        os.remove(temp_img_name)

    return total_data

# ==========================================
# 3. 실제 실행 영역
# ==========================================
if __name__ == "__main__":
    # 분석할 PDF 파일 이름을 여기에 적으세요 (같은 폴더에 있어야 함)
    target_pdf = "inf_exam_sample.pdf" 
    
    final_result = start_parsing(target_pdf)

    if final_result:
        # 결과를 'result.json'이라는 파일로 저장합니다.
        with open("result.json", "w", encoding="utf-8") as f:
            json.dump(final_result, f, ensure_ascii=False, indent=4)
        
        print("\n==========================================")
        print(f"분석 완료! 총 {len(final_result)}개의 문제를 찾았습니다.")
        print("결과는 'result.json' 파일에 저장되었습니다.")
        print("==========================================")