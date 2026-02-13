package com.example.pythonttsapp

/* =========================================================
   UiState.kt
   =========================================================

   📌 UI 상태 데이터 모델

   MVVM에서 UI는 "상태(State)"만 보고 화면을 그림

   Activity는
   - 상태를 직접 만들지 않음
   - ViewModel이 만든 상태를 관찰만 함

   즉

   ViewModel → UiState 생성
   Activity → UiState 구독 (observe)

========================================================= */

data class UiState(

    /* 화면에 표시할 전체 텍스트 */
    val text: String = "",

    /* 파일 로딩 중인지 여부 (로딩 표시용) */
    val isLoading: Boolean = false,

    /* 현재 읽고 있는 문장 / 문단 index */
    val currentIndex: Int = 0,

    /* 재생 중인지 여부 */
    val isPlaying: Boolean = false
)
