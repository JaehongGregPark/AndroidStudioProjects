package com.example.audioscript.domain.repository

import android.net.Uri

/**
 * Domain Layer Repository 인터페이스
 *
 * - 데이터의 출처를 모른다.
 * - 무엇을 할 수 있는지만 정의한다.
 * - Data Layer가 이 인터페이스를 구현한다.
 */
interface TextRepository {

    /**
     * 외부 파일에서 텍스트를 읽는다.
     */
    suspend fun loadText(uri: Uri): String

    /**
     * 텍스트 번역
     */
    suspend fun translate(text: String): String

    /**
     * 소설 생성
     */
    suspend fun generateStory(
        title: String,
        isKorean: Boolean
    ): String
}
