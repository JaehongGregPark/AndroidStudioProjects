package com.example.audioscript.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.audioscript.data.pdf.PdfGenerator
import com.example.audioscript.domain.repository.TextRepository

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
 * Main ViewModel
 * ===============================
 *
 * 역할:
 *
 * ✔ UI 상태 관리
 * ✔ UseCase 호출
 * ✔ Repository 접근
 * ✔ TXT / PDF / TTS 제어
 */
@HiltViewModel
class MainViewModel @Inject constructor(

    private val loadTextUseCase: LoadTextUseCase,

    private val translateUseCase: TranslateUseCase,

    private val generateStoryUseCase: GenerateStoryUseCase,

    private val repository: TextRepository,   // ✅ 추가

    private val pdfGenerator: PdfGenerator,

    private val ttsManager: TTSManager

) : ViewModel() {

    // 상태

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    private val _speechRate = MutableStateFlow(1f)
    val speechRate: StateFlow<Float> = _speechRate

    private val _pitch = MutableStateFlow(1f)
    val pitch: StateFlow<Float> = _pitch


    init {

        viewModelScope.launch {

            _text.value = loadTextUseCase()

        }

    }


    fun updateText(newText: String) {

        _text.value = newText

    }


    fun translate() {

        viewModelScope.launch {

            _text.value =
                translateUseCase(_text.value)

        }

    }


    fun generateStory(

        title: String,

        isKorean: Boolean

    ) {

        viewModelScope.launch {

            _text.value =
                generateStoryUseCase(title, isKorean)

        }

    }


    /**
     * ✅ TXT 저장 추가
     */
    fun exportTxt(

        fileName: String

    ) {

        viewModelScope.launch {

            repository.exportTxt(

                fileName,

                _text.value

            )

        }

    }



    /**
     * PDF 저장
     */
    fun exportPdf(

        fileName: String

    ) {

        pdfGenerator.createPdf(

            _text.value,

            fileName

        )

    }


    fun setSpeechRate(value: Float) {

        _speechRate.value = value

    }


    fun setPitch(value: Float) {

        _pitch.value = value

    }


    fun speak() {

        ttsManager.speak(

            _text.value,

            _speechRate.value,

            _pitch.value

        )

    }


    override fun onCleared() {

        ttsManager.shutdown()

        super.onCleared()

    }

}