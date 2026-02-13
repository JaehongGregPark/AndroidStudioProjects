package com.example.pythonttsapp

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/* =========================================================
   MainViewModel.kt
   =========================================================

   📌 UI 상태 관리자

   역할:
   - 파일 로딩
   - 문장 / 문단 분리
   - 현재 읽기 위치 관리
   - UI 상태 생성

   Activity는 ViewModel에 명령만 전달
   화면 데이터는 ViewModel이 제공

========================================================= */

class MainViewModel(
    private val repo: TtsRepository = TtsRepository()
) : ViewModel() {

    /* =====================================================
       UI 상태 저장 (StateFlow)
       ===================================================== */

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    /* 읽기 단위 리스트 */
    private var readingUnits = listOf<String>()

    /* 문장 모드 여부 */
    private var sentenceMode = true

    /* =====================================================
       파일 로드
       ===================================================== */
    fun loadFile(resolver: ContentResolver, uri: Uri) {

        viewModelScope.launch(Dispatchers.IO) {

            // 로딩 시작
            _uiState.value = _uiState.value.copy(isLoading = true)

            val text =
                if (repo.isPdf(resolver, uri))
                    repo.readPdf(resolver, uri)
                else
                    repo.readText(resolver, uri)

            // 읽기 단위 생성
            readingUnits =
                if (sentenceMode)
                    repo.splitSentences(text)
                else
                    repo.splitParagraphs(text)

            // UI 상태 업데이트
            _uiState.value = UiState(
                text = text,
                currentIndex = 0,
                isLoading = false
            )
        }
    }

    /* =====================================================
       현재 읽을 텍스트 반환
       ===================================================== */
    fun getCurrentUnit(): String? {

        val index = _uiState.value.currentIndex

        if (index >= readingUnits.size) return null

        return readingUnits[index]
    }

    /* =====================================================
       다음 문장 이동
       ===================================================== */
    fun next() {
        _uiState.value =
            _uiState.value.copy(
                currentIndex = _uiState.value.currentIndex + 1
            )
    }

    /* =====================================================
       특정 위치 이동
       ===================================================== */
    fun setIndex(i: Int) {
        _uiState.value = _uiState.value.copy(currentIndex = i)
    }

    /* =====================================================
       읽기 모드 변경
       ===================================================== */
    fun setSentenceMode(sentence: Boolean) {
        sentenceMode = sentence
    }
}
