package com.example.scriptaudio.tts

import android.content.Context
import android.speech.tts.TextToSpeech

/**
 * TTS 관리 클래스
 *
 * ViewModel → TTSManager 호출 구조
 *
 */
class TTSManager(

    context: Context

) {

    private val tts = TextToSpeech(

        context

    ) {

        /**
         * 초기화 완료
         */

    }


    /**
     * TTS 실행 함수
     */
    fun speak(

        text: String,

        rate: Float,

        pitch: Float

    ) {

        /**
         * 속도 설정
         */
        tts.setSpeechRate(rate)


        /**
         * Pitch 설정
         */
        tts.setPitch(pitch)


        /**
         * 음성 출력
         */
        tts.speak(

            text,

            TextToSpeech.QUEUE_FLUSH,

            null,

            null

        )

    }

}