package com.example.pythonttsmvvmapp.data

/**
 * 최근 열었던 파일 정보를 담는 데이터 클래스
 */
data class RecentFile(
    val name: String,   // 사용자에게 보여줄 파일 이름
    val uri: String    // 다시 열기 위한 Uri 문자열
)
