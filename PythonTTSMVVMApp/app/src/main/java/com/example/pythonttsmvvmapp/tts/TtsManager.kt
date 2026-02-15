package com.example.pythonttsmvvmapp.tts


import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 전역에서 사용하는 TTS 엔진 매니저
 *
 * 특징:
 * - Hilt를 통해 Singleton 으로 관리
 * - TTS 초기화 이전 speak 요청을 안전하게 큐에 저장
 * - 초기화 완료되면 자동으로 대기중인 문장을 재생
 * - ApplicationContext 사용으로 메모리 누수 방지
 */
@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    /** TTS 엔진 준비 여부 */
    private var isReady = false

    /** 초기화 전에 들어온 발화 요청을 임시 저장 */
    private val pendingQueue = mutableListOf<String>()

    init {
        tts = TextToSpeech(context, this)
    }

    /**
     * TTS 엔진 초기화 완료 콜백
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true

            tts?.language = Locale.KOREAN

            // 대기 중이던 요청 처리
            pendingQueue.forEach {
                tts?.speak(it, TextToSpeech.QUEUE_ADD, null, "tts")
            }
            pendingQueue.clear()
        }
    }

    /**
     * 음성 출력 요청
     *
     * 초기화 전이면 큐에 저장하고,
     * 준비 완료되면 자동 실행됨.
     */
    fun speak(text: String) {
        if (!isReady) {
            pendingQueue.add(text)
            return
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts")
    }

    /**
     * 현재 재생 중지
     */
    fun stop() {
        tts?.stop()
    }

    /**
     * TTS 리소스 해제
     * (앱 완전 종료 시 필요하면 사용)
     */
    fun shutdown() {
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
