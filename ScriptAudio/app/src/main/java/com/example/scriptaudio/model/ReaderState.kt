package com.example.scriptaudio.model.reader

/**
 * ReaderState
 *
 * Reader UI 상태 모델
 *
 * ViewModel → UI 상태 전달
 */

data class ReaderState(

    /**
     * 전체 텍스트
     */
    val text: String = "",

    /**
     * 현재 읽는 문장 index
     */
    val currentSentence: Int = 0,

    /**
     * TTS 실행 여부
     */
    val isReading: Boolean = false,

    /**
     * 현재 페이지
     */
    val currentPage: Int = 0,

    /**
     * 전체 페이지
     */
    val totalPages: Int = 0

)