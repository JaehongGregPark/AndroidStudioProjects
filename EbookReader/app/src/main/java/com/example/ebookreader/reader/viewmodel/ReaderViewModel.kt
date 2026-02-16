package com.example.ebookreader.reader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebookreader.reader.model.ReaderUiState
import com.example.ebookreader.reader.usecase.LoadBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 📌 ReaderViewModel (UI 전용 ViewModel)
 *
 * 역할:
 * ✔ UI 상태 관리
 * ✔ UseCase 호출
 *
 * 하지 않는 것:
 * ❌ 파일 파싱 로직
 * ❌ Repository 직접 접근
 * ❌ Android Context 사용
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val loadBookUseCase: LoadBookUseCase
) : ViewModel() {

    /**
     * 📌 화면에서 관찰하는 UI 상태
     */
    var uiState: ReaderUiState = ReaderUiState()
        private set

    /**
     * 📖 전자책 로드
     *
     * @param bookPath 사용자가 선택한 파일 경로
     */
    fun loadBook(bookPath: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val result = loadBookUseCase(bookPath)

            uiState = if (result.isSuccess) {
                uiState.copy(
                    isLoading = false,
                    pages = result.getOrDefault(emptyList()),
                    currentPage = 0,
                    errorMessage = null
                )
            } else {
                uiState.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    /**
     * ▶ 다음 페이지
     */
    fun nextPage() {
        if (uiState.currentPage < uiState.pages.lastIndex) {
            uiState = uiState.copy(
                currentPage = uiState.currentPage + 1
            )
        }
    }

    /**
     * ◀ 이전 페이지
     */
    fun previousPage() {
        if (uiState.currentPage > 0) {
            uiState = uiState.copy(
                currentPage = uiState.currentPage - 1
            )
        }
    }
}
