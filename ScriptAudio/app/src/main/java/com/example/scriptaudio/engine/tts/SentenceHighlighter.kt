package com.example.scriptaudio.engine.tts

/**
 * SentenceHighlighter
 *
 * TTS 진행에 따라
 * 현재 읽는 문장을 하이라이트
 */

class SentenceHighlighter {

    /**
     * 텍스트를 문장 단위로 분리
     */
    fun splitSentences(text: String): List<String> {

        return text.split(
            ".",
            "?",
            "!"
        ).map { it.trim() }
            .filter { it.isNotEmpty() }

    }

    /**
     * 현재 문장 인덱스 계산
     */
    fun getSentenceIndex(

        text: String,
        position: Int

    ): Int {

        val sentences = splitSentences(text)

        var count = 0

        sentences.forEachIndexed { index, sentence ->

            count += sentence.length

            if (position <= count) {

                return index

            }

        }

        return 0

    }

}