package com.example.audioscript.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.audioscript.data.pdf.PdfGenerator

import com.example.audioscript.domain.usecase.GenerateStoryUseCase
import com.example.audioscript.domain.usecase.LoadTextUseCase
import com.example.audioscript.domain.usecase.TranslateUseCase
import com.example.audioscript.domain.usecase.ExportTxtUseCase

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
 * 역할:
 *
 * ✔ UI 상태 관리
 * ✔ UseCase 호출
 * ✔ TTS 제어
 * ✔ PDF 저장
 * ✔ TXT 저장
 *
 * 규칙:
 *
 * ✔ Repository 직접 접근 금지
 * ✔ 모든 비즈니스 로직은 UseCase 통해 처리
 *
 */
@HiltViewModel
class MainViewModel @Inject constructor(

    private val loadTextUseCase: LoadTextUseCase,

    private val translateUseCase: TranslateUseCase,

    private val generateStoryUseCase: GenerateStoryUseCase,

    private val exportTxtUseCase: ExportTxtUseCase,

    private val pdfGenerator: PdfGenerator,

    private val ttsManager: TTSManager

) : ViewModel() {


    // ===============================
    // UI STATE
    // ===============================


    /**
     * 텍스트 상태
     */
    private val _text = MutableStateFlow("")

    val text: StateFlow<String> = _text



    /**
     * TTS 속도
     */
    private val _speechRate = MutableStateFlow(1f)

    val speechRate: StateFlow<Float> = _speechRate



    /**
     * TTS 피치
     */
    private val _pitch = MutableStateFlow(1f)

    val pitch: StateFlow<Float> = _pitch



    // ===============================
    // INIT
    // ===============================

    init {

        viewModelScope.launch {

            _text.value = loadTextUseCase()

        }

    }



    // ===============================
    // TEXT
    // ===============================

    /**
     * 텍스트 업데이트
     */
    fun updateText(newText: String) {

        _text.value = newText

    }



    // ===============================
    // TRANSLATE
    // ===============================

    fun translate() {

        viewModelScope.launch {

            _text.value = translateUseCase(

                _text.value

            )

        }

    }



    // ===============================
    // GENERATE STORY
    // ===============================

    fun generateStory(

        title: String,

        isKorean: Boolean

    ) {

        viewModelScope.launch {

            _text.value = generateStoryUseCase(

                title,
                isKorean

            )

        }

    }



    // ===============================
    // EXPORT TXT  ⭐ 추가된 부분
    // ===============================

    fun exportTxt(

        fileName: String

    ) {

        viewModelScope.launch {

            exportTxtUseCase(

                fileName,
                _text.value

            )

        }

    }



    // ===============================
    // EXPORT PDF
    // ===============================

    /**
     * 인프라 영역이므로 직접 호출 허용
     */
    fun exportPdf(

        fileName: String

    ) {

        pdfGenerator.createPdf(

            _text.value,
            fileName

        )

    }



    // ===============================
    // TTS CONTROL
    // ===============================

    fun setSpeechRate(value: Float) {

        _speechRate.value = value

    }


    fun setPitch(value: Float) {

        _pitch.value = value

    }



    fun speak() {

        ttsManager.speak(

            text = _text.value,

            rate = _speechRate.value,

            pitch = _pitch.value

        )

    }



    // ===============================
    // DESTROY
    // ===============================

    override fun onCleared() {

        ttsManager.shutdown()

        super.onCleared()

    }

}
