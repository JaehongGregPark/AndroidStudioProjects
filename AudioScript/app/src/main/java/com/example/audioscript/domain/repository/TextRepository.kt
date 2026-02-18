package com.example.audioscript.domain.repository

/**
 * ===============================
 * Domain Layer - Repository Interface
 * ===============================
 *
 * ✔ Domain은 외부 구현(Data, Android, MLKit 등)을 모른다.
 * ✔ 오직 '기능의 계약(Contract)'만 정의한다.
 *
 * Clean Architecture 규칙:
 *  - Domain은 어떤 프레임워크에도 의존하지 않는다.
 *  - 구현은 Data Layer에서 수행한다.
 */
interface TextRepository {

    /**
     * 초기 텍스트 로드
     * (향후 DB / 파일 / 네트워크 등 확장 가능)
     */
    suspend fun loadText(): String

    /**
     * 텍스트 번역
     * 실제 구현은 MLKit 사용 (Data Layer)
     */
    suspend fun translate(text: String): String

    /**
     * 소설 생성
     * 현재는 더미 로직
     * 추후 AI API 연결 가능
     */
    suspend fun generateStory(
        title: String,
        isKorean: Boolean
    ): String
}
