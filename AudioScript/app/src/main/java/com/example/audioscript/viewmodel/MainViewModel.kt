package com.example.audioscript.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioscript.data.pdf.PdfGenerator
import com.example.audioscript.domain.usecase.GenerateStoryUseCase
import com.example.audioscript.domain.usecase.LoadTextUseCase
import com.example.audioscript.domain.usecase.TranslateUseCase
import com.example.audioscript.tts.engine.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ===============================
 * Presentation Layer - ViewModel
 * ===============================
 *
 * ✔ UI 상태 관리
 * ✔ UseCase 호출
 * ✔ Android 생명주기 대응
 *
 * 절대 Repository에 직접 접근하지 않는다.
 * (Clean Architecture 핵심 규칙)
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val loadTextUseCase: LoadTextUseCase,
    private val translateUseCase: TranslateUseCase,
    private val generateStoryUseCase: GenerateStoryUseCase,
    private val pdfGenerator: PdfGenerator,
    private val ttsManager: TTSManager
) : ViewModel() {

    // ===============================
    // 단일 UI 상태 (Single Source of Truth)
    // ===============================

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    private val _speechRate = MutableStateFlow(1f)
    val speechRate: StateFlow<Float> = _speechRate

    private val _pitch = MutableStateFlow(1f)
    val pitch: StateFlow<Float> = _pitch

    /**
     * 초기 데이터 로드
     */
    init {
        viewModelScope.launch {
            _text.value = loadTextUseCase()
        }
    }

    /**
     * UI 입력 반영
     */
    fun updateText(newText: String) {
        _text.value = newText
    }

    /**
     * 번역 실행
     */
    fun translate() {
        viewModelScope.launch {
            _text.value = translateUseCase(_text.value)
        }
    }

    /**
     * 소설 생성
     */
    fun generateStory(title: String, isKorean: Boolean) {
        viewModelScope.launch {
            _text.value =
                generateStoryUseCase(title, isKorean)
        }
    }

    /**
     * PDF 저장
     * (Data Layer 직접 호출 허용 - 인프라 영역)
     */
    fun exportPdf(fileName: String) {
        pdfGenerator.createPdf(_text.value, fileName)
    }

    /**
     * TTS 설정 변경
     */
    fun setSpeechRate(value: Float) {
        _speechRate.value = value
    }

    fun setPitch(value: Float) {
        _pitch.value = value
    }

    /**
     * 음성 출력
     */
    fun speak() {
        ttsManager.speak(
            text = _text.value,
            rate = _speechRate.value,
            pitch = _pitch.value
        )
    }

    override fun onCleared() {
        ttsManager.shutdown()
        super.onCleared()
    }
}
