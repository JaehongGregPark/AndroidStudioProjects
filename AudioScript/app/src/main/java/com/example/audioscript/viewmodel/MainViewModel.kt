package com.example.audioscript.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioscript.domain.usecase.LoadTextUseCase
import com.example.audioscript.domain.usecase.TranslateUseCase
import com.example.audioscript.domain.usecase.GenerateStoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 메인 화면 ViewModel
 *
 * - 파일 로딩
 * - 번역
 * - 소설 생성
 *
 * UI 상태만 관리한다.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val loadTextUseCase: LoadTextUseCase,
    private val translateUseCase: TranslateUseCase,
    private val generateStoryUseCase: GenerateStoryUseCase
) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText

    /**
     * 파일 로딩
     */
    fun load(uri: Uri) {
        viewModelScope.launch {
            try {
                _fileName.value = uri.lastPathSegment ?: "선택된 파일"
                _text.value = loadTextUseCase(uri)
            } catch (e: Exception) {
                _text.value = "파일 로딩 실패: ${e.message}"
            }
        }
    }

    /**
     * 번역 실행
     */
    fun translate() {
        viewModelScope.launch {
            _translatedText.value =
                translateUseCase(_text.value)
        }
    }

    /**
     * 소설 생성
     */
    fun generateStory(
        title: String,
        isKorean: Boolean
    ) {
        viewModelScope.launch {
            _text.value =
                generateStoryUseCase(title, isKorean)
        }
    }
}
