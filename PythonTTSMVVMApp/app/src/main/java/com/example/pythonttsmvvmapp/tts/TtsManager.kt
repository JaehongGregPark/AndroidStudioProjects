package com.example.pythonttsmvvmapp.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 전체에서 사용하는 TTS 엔진 관리자
 */
@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    /** 실제 TTS 객체 */
    private var tts: TextToSpeech? = null

    /** 준비 완료 여부 */
    private var ready = false

    /** 현재 읽는 위치를 전달할 콜백 */
    private var rangeListener: ((Int, Int) -> Unit)? = null

    init {
        tts = TextToSpeech(context, this)

        /**
         * 읽는 위치 변화 감지
         */
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {}

            override fun onRangeStart(
                utteranceId: String?,
                start: Int,
                end: Int,
                frame: Int
            ) {
                rangeListener?.invoke(start, end)
            }
        })
    }

    /** 초기화 완료 */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ready = true
            tts?.language = Locale.KOREAN
        }
    }

    /** 읽기 */
    fun speak(text: String) {
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts")
    }

    /** 일시정지 (stop 으로 처리) */
    fun pause() {
        tts?.stop()
    }

    /** 정지 */
    fun stop() {
        tts?.stop()
    }

    /** 종료 */
    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
    }

    /** 범위 리스너 등록 */
    fun setOnRangeChanged(listener: (Int, Int) -> Unit) {
        rangeListener = listener
    }
}
