package com.example.scriptaudio.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scriptaudio.engine.tts.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * MainViewModel
 *
 * - 앱 전체 상태를 관리하는 ViewModel
 * - MainScreen (Reader/Translation/Library)와 ReaderScreen 상태 공유
 * - 파일 관리, 텍스트 관리, TTS 재생, 번역 상태 관리
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // -----------------------------
    // Reader/Translation 상태
    // -----------------------------
    private val _originalText = MutableStateFlow("")
    val originalText: StateFlow<String> = _originalText

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating

    /**
    * 번역 방향 상태
    * true = 영어 → 한글
    * false = 한글 → 영어
    */
    private val _isEnglish = MutableStateFlow(true)
    val isEnglish: StateFlow<Boolean> = _isEnglish

    // -----------------------------
    // Library 상태 (파일 목록)
    // -----------------------------
    private val _fileList = MutableStateFlow<List<File>>(emptyList())
    val fileList: StateFlow<List<File>> = _fileList

    // -----------------------------
    // TTS 관리
    // -----------------------------
    private val tts = TTSManager(context)

    // -----------------------------
    // 텍스트 업데이트
    // -----------------------------
    fun updateScript(text: String) {
        _originalText.value = text
    }

    // -----------------------------
    // 텍스트 번역 (예시: 현재는 단순 reverse)
    // 실제 번역 모델을 적용 가능
    // -----------------------------
    fun translate() {
        viewModelScope.launch {
            _isTranslating.value = true

            val text = _originalText.value
            try {
                val result = translateText(
                    text,
                    if (_isEnglish.value) "en" else "ko",
                    if (_isEnglish.value) "ko" else "en"
                     )

                _translatedText.value = result

                // 번역 방향 토글
                _isEnglish.value = !_isEnglish.value

                } catch (e: Exception) {
                    e.printStackTrace()
                    _translatedText.value = "번역 실패"
                }

                _isTranslating.value = false
        }
    }

    /**
    * 실제 번역 함수 (Google 무료 endpoint)
    */
    private suspend fun translateText(
        text: String,
        from: String,
        to: String
        ): String {
        return try {
            val url = java.net.URL(
                "https://translate.googleapis.com/translate_a/single?client=gtx" +
                       "&sl=$from&tl=$to&dt=t&q=" +
                        java.net.URLEncoder.encode(text, "UTF-8")
                        )

            val result = url.readText()

            org.json.JSONArray(result)
               .getJSONArray(0)
               .getJSONArray(0)
               .getString(0)

            } catch (e: Exception) {
                        e.printStackTrace()
                        "번역 오류"
            }

    }

    // -----------------------------
    // TTS 실행
    // -----------------------------
    fun speak() {
        val text = _originalText.value
        if (text.isNotBlank()) tts.speak(text)
    }

    // -----------------------------
    // 파일 열기
    // -----------------------------
    fun openFile(file: File) {
        viewModelScope.launch {
            try {
                val content = file.readText()
                _originalText.value = content
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // -----------------------------
    // 파일 삭제
    // -----------------------------
    fun deleteFile(file: File) {
        _fileList.value = _fileList.value.filter { it != file }
    }

    // -----------------------------
    // 앱 초기 파일 목록 로드 (예시)
    // -----------------------------
    fun loadFiles() {
        // TODO: 실제 앱 파일 로딩 구현
        _fileList.value = listOf() // 빈 리스트
    }
}