package com.example.scriptaudio.engine.tts

/**
 * TTSManager
 *
 * Android TextToSpeech wrapper
 *
 * 기능
 * - 텍스트 읽기
 * - 속도 조절
 * - 음성 정지
 */

import android.speech.tts.TextToSpeech
import android.content.Context
import java.util.Locale

class TTSManager(

    private val context: Context

) {

    private var tts: TextToSpeech? = null

    init {

        tts = TextToSpeech(context) {

            if (it == TextToSpeech.SUCCESS) {

                tts?.language = Locale.US

                tts?.setSpeechRate(1.0f)

            }

        }

    }


    fun setSpeed(speed: Float) {
        tts?.setSpeechRate(speed)
    }


    /**
     * 텍스트 읽기
     */
    fun speak(text: String) {

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "ScriptAudio"
        )

    }

    /**
     * 읽기 중지
     */
    fun stop() {

        tts?.stop()

    }

}