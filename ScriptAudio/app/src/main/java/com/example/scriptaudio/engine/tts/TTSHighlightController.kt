package com.example.scriptaudio.engine.tts

/**
 * TTSHighlightController
 *
 * TTS 읽기와
 * 문장 하이라이트 동기화
 */

import kotlinx.coroutines.delay

class TTSHighlightController(

    private val tts: TTSManager,

    private val highlighter: SentenceHighlighter

) {

    /**
     * 문장 단위 TTS
     */

    suspend fun speakWithHighlight(

        text: String,

        onSentenceChanged: (Int) -> Unit

    ) {

        val sentences =

            highlighter.splitSentences(text)

        sentences.forEachIndexed { index, sentence ->

            onSentenceChanged(index)

            tts.speak(sentence)

            /**
             * TTS 재생 시간 대기
             */

            delay(

                (sentence.length * 40).toLong()

            )

        }

    }

}