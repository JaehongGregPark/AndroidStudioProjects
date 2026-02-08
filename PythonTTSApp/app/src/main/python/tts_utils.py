import re

def split_korean_english(text):
    result = []
    pattern = re.compile(r'([가-힣]+)|([a-zA-Z]+)')
    for match in pattern.finditer(text):
        if match.group(1):
            result.append(["ko", match.group(1)])
        elif match.group(2):
            result.append(["en", match.group(2)])
    return result