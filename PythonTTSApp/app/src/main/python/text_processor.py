def process_text(text):
    text = text.strip()

    if not text:
        return "none", ""

    for ch in text:
        if '가' <= ch <= '힣':
            return "ko", text

    return "en", text