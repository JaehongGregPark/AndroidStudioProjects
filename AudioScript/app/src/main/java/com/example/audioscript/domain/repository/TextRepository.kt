package com.example.audioscript.domain.repository

/**
 * ===============================
 * Domain Layer - Repository Interface
 * ===============================
 *
 * ✔ Domain은 구현을 모른다.
 * ✔ 기능 정의만 담당한다.
 *
 * 구현 위치:
 * Data Layer → TextRepositoryImpl
 */
interface TextRepository {

    /**
     * 앱 시작 시 텍스트 로드
     *
     * 향후 확장 가능:
     * - Room DB
     * - 내부 파일
     * - 클라우드
     */
    suspend fun loadText(): String


    /**
     * 텍스트 번역
     *
     * 구현:
     * - MLKit
     * - Google Translate API
     */
    suspend fun translate(text: String): String


    /**
     * 소설 생성
     *
     * 향후 확장:
     * - OpenAI
     * - Gemini
     */
    suspend fun generateStory(
        title: String,
        isKorean: Boolean
    ): String


    /**
     * TXT 파일 저장 기능
     *
     * Clean Architecture 원칙:
     * Domain은 파일 시스템을 모르지만
     * 기능 정의는 가능하다.
     */
    suspend fun exportTxt(

        fileName: String,

        content: String

    )
}