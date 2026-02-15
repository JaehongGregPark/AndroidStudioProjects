package com.example.pythonttsmvvmapp.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pythonttsmvvmapp.data.parser.PdfParser
import com.example.pythonttsmvvmapp.data.parser.TxtParser
import com.example.pythonttsmvvmapp.tts.TtsManager
import com.example.pythonttsmvvmapp.tts.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI 와 데이터(TTS, 파일)를 연결하는 계층
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    private val txtParser: TxtParser,
    private val pdfParser: PdfParser
) : ViewModel() {

    /** 현재 화면에 보여줄 텍스트 */
    val text = mutableStateOf("")

    /** 재생 상태 */
    val state = mutableStateOf<TtsState>(TtsState.Idle)

    /** 하이라이트 범위 */
    val highlightStart = mutableStateOf(-1)
    val highlightEnd = mutableStateOf(-1)

    init {
        /**
         * TTS 진행 위치 수신
         */
        ttsManager.setOnRangeChanged { s, e ->
            highlightStart.value = s
            highlightEnd.value = e
        }
    }

    /**
     * 파일 열기
     * 확장자에 따라 파서 선택
     */
    fun openFile(uri: Uri) {
        viewModelScope.launch {
            val result = when {
                uri.toString().endsWith(".txt") -> txtParser.parse(uri)
                uri.toString().endsWith(".pdf") -> pdfParser.parse(uri)
                else -> "지원하지 않는 파일 형식입니다."
            }
            text.value = result
        }
    }

    fun speak() {
        if (text.value.isBlank()) return
        ttsManager.speak(text.value)
        state.value = TtsState.Speaking(IntRange(0, 0))
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
