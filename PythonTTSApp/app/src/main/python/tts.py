import re
import time
import pyttsx3

engine = pyttsx3.init()

def speak_text(text, rate=170):
    engine.setProperty('rate', rate)

    # 문장 단위로 분리
    sentences = re.split(r'(?<=[.!?])\s+', text)

    for sentence in sentences:
        sentence = sentence.strip()
        if not sentence:
            continue

        engine.say(sentence)
        engine.runAndWait()

        # 문장 사이 쉬는 시간 (초)
        time.sleep(0.3)
