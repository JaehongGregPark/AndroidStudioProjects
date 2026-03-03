package com.example.worldradio.ui.state

/**
 * 화면 상태 관리용 Sealed Class
 *
 * Success  → 데이터 성공
 * Loading  → 로딩 중
 * Error    → 에러 발생
 */
sealed class UiState<out T> {

    object Loading : UiState<Nothing>()

    data class Success<T>(
        val data: T
    ) : UiState<T>()

    data class Error(
        val message: String
    ) : UiState<Nothing>()
}