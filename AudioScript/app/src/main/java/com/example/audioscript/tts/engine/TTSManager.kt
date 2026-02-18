package com.example.audioscript.tts.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

/**
 * Android TextToSpeech 엔진 래퍼 클래스
 *
 * 책임:
 * - 엔진 초기화
 * - 언어 자동 감지
 * - 속도/피치 설정
 * - 자원 해제
 */
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) {}
    }

    /**
     * 텍스트 음성 출력
     */
    fun speak(
        text: String,
        rate: Float,
        pitch: Float
    ) {

        if (text.isBlank()) return

        val isKorean = text.contains(Regex("[가-힣]"))
        val locale = if (isKorean) {
            Locale.KOREAN
        } else {
            Locale.US
        }

        tts?.setLanguage(locale)
        tts?.setSpeechRate(rate)
        tts?.setPitch(pitch)

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
    }

    /**
     * 메모리 누수 방지
     */
    fun shutdown() {
        tts?.shutdown()
    }
}
