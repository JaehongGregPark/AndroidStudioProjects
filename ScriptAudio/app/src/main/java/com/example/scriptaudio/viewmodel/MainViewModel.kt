package com.example.scriptaudio.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scriptaudio.data.SettingsDataStore
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
 * - 앱 전체 상태 관리
 * - Reader / Settings / TTS 공유
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
    // TTS 속도 상태 (추가)
    // -----------------------------
    private val _ttsSpeed = MutableStateFlow(1.0f)
    val ttsSpeed: StateFlow<Float> = _ttsSpeed


    private val _fontSize = MutableStateFlow(18f)
   val fontSize: StateFlow<Float> = _fontSize

    private val _scrollSpeed = MutableStateFlow(1f)
    val scrollSpeed: StateFlow<Float> = _scrollSpeed

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode

    private val _currentSentence = MutableStateFlow(0)
    val currentSentence = _currentSentence

    private val settings = SettingsDataStore(context)

    fun setFontSize(v: Float) {
        _fontSize.value = v
        viewModelScope.launch {
            settings.setFontSize(v)
        }
    }

    fun setScrollSpeed(v: Float) {
        _scrollSpeed.value = v
        viewModelScope.launch {
            settings.setScrollSpeed(v)
        }
    }

    fun setDarkMode(v: Boolean) {
        _darkMode.value = v
        viewModelScope.launch {
            settings.setDarkMode(v)
        }
    }


    fun setCurrentSentence(i:Int){
        _currentSentence.value = i
    }
    /**
     * TTS 속도 변경
     */
    fun setTtsSpeed(speed: Float) {
        _ttsSpeed.value = speed
        tts.setSpeed(speed)   // TTSManager에도 반영
    }

    init {
        viewModelScope.launch {
            settings.ttsSpeed.collect { _ttsSpeed.value = it }
        }

        viewModelScope.launch {
            settings.fontSize.collect { _fontSize.value = it }
        }

        viewModelScope.launch {
            settings.scrollSpeed.collect { _scrollSpeed.value = it }
        }

        viewModelScope.launch {
            settings.darkMode.collect { _darkMode.value = it }
        }
    }

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
    // 번역
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

                _isEnglish.value = !_isEnglish.value

            } catch (e: Exception) {
                e.printStackTrace()
                _translatedText.value = "번역 실패"
            }

            _isTranslating.value = false
        }
    }


    /**
     * 번역 함수
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
        val speed = _ttsSpeed.value

        if (text.isNotBlank()) {
            tts.setSpeed(speed)
            tts.speak(text)
        }
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
    // 파일 목록 로드
    // -----------------------------
    fun loadFiles() {
        _fileList.value = listOf()
    }
}