package com.example.ebookreader.domain.model

/**
 * 파일 파싱 결과를 표현하는 도메인 모델
 *
 * UI / ViewModel / UseCase 에서 사용
 * Android API 와 무관한 순수 Kotlin 데이터 클래스
 */
data class ParsedFile(
    val name: String,   // 파일 이름
    val text: String    // 파일 전체 텍스트
)
