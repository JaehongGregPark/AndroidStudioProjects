package com.example.pythonttsmvvmapp.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 전체에서 사용하는 TTS 엔진 관리 클래스
 */
@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext context: Context
) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        tts.language = Locale.KOREAN
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts")
    }

    fun stop() = tts.stop()
}