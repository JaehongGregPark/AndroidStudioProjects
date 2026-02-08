import re

def split_korean_english(text):
    """
    한글/영어 덩어리로 분리
    """
    pattern = re.compile(r'[가-힣]+|[a-zA-Z]+|[^a-zA-Z가-힣]+')
    parts = pattern.findall(text)

    result = []
    for part in parts:
        if re.search(r'[가-힣]', part):
            result.append(("ko", part.strip()))
        elif re.search(r'[a-zA-Z]', part):
            result.append(("en", part.strip()))

    return result