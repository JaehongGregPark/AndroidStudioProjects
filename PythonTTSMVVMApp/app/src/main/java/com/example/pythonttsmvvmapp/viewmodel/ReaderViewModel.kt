package com.example.pythonttsmvvmapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pythonttsmvvmapp.tts.TtsManager
import com.example.pythonttsmvvmapp.tts.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * UI 와 TTS 사이를 연결
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val ttsManager: TtsManager
) : ViewModel() {

    /** 재생 상태 */
    val state = mutableStateOf<TtsState>(TtsState.Idle)

    /** 현재 텍스트 */
    val text = mutableStateOf("")

    /** 하이라이트 범위 */
    val highlightStart = mutableStateOf(-1)
    val highlightEnd = mutableStateOf(-1)

    init {
        /**
         * TTS 가 읽는 위치 알려주면 저장
         */
        ttsManager.setOnRangeChanged { start, end ->
            highlightStart.value = start
            highlightEnd.value = end
            state.value = TtsState.Speaking(start..end)
        }
    }

    fun setText(newText: String) {
        text.value = newText
    }

    fun speak() {
        ttsManager.speak(text.value)
        state.value = TtsState.Speaking(0..0)
    }

    fun pause() {
        ttsManager.pause()
        state.value = TtsState.Paused
    }

    fun stop() {
        ttsManager.stop()
        state.value = TtsState.Idle
        highlightStart.value = -1
        highlightEnd.value = -1
    }
}
