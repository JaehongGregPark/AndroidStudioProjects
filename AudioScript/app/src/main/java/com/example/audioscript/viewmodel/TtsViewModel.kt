package com.example.audioscript.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.audioscript.tts.engine.TTSManager

/**
 * TtsViewModel
 *
 * ✔ UI에서 사용하는 TTS 설정 상태 관리
 * ✔ 실제 음성 엔진은 TTSManager가 담당
 *
 * 이 ViewModel은 "음성 설정 상태"를 관리한다.
 */
class TtsViewModel(application: Application)
    : AndroidViewModel(application) {

    // 실제 TTS 엔진 제어 객체
    private val ttsManager = TTSManager(application)

    // 말하기 속도 상태
    private val _speechRate = MutableStateFlow(0.95f)
    val speechRate: StateFlow<Float> = _speechRate

    // 음성 톤 상태
    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch

    /**
     * 속도 변경
     */
    fun setSpeechRate(value: Float) {
        _speechRate.value = value
    }

    /**
     * 피치 변경
     */
    fun setPitch(value: Float) {
        _pitch.value = value
    }

    /**
     * 실제 음성 재생
     */
    fun speak(text: String) {
        ttsManager.speak(text, _speechRate.value, _pitch.value)
    }

    override fun onCleared() {
        ttsManager.shutdown()
        super.onCleared()
    }
}
