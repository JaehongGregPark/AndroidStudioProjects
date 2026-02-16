package com.example.ebookreader.reader.model

/**
 * 📌 Reader 화면 상태
 */
data class ReaderUiState(
    val isLoading: Boolean = false,
    val pages: List<String> = emptyList(),
    val currentPage: Int = 0,
    val errorMessage: String? = null
)
