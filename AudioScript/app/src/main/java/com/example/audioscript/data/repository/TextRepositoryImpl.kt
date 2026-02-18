package com.example.audioscript.data.repository

import com.example.audioscript.data.ml.MLKitTranslator
import com.example.audioscript.domain.repository.TextRepository
import com.google.mlkit.nl.translate.TranslateLanguage
import javax.inject.Inject

/**
 * ===============================
 * Data Layer - Repository 구현체
 * ===============================
 *
 * ✔ 실제 구현 담당
 * ✔ 외부 라이브러리 사용 (MLKit 등)
 * ✔ Domain Layer는 이 구현을 모른다.
 *
 * 의존 방향:
 * Presentation → Domain → Data
 */
class TextRepositoryImpl @Inject constructor(
    private val translator: MLKitTranslator
) : TextRepository {

    override suspend fun loadText(): String {
        // 현재는 빈 문자열 반환
        // 추후 Room DB / 파일 저장소 연결 가능
        return ""
    }

    override suspend fun translate(text: String): String {

        // 한글 포함 여부로 번역 방향 결정
        val isKorean = text.contains(Regex("[가-힣]"))

        return if (isKorean) {
            translator.translate(
                text,
                TranslateLanguage.KOREAN,
                TranslateLanguage.ENGLISH
            )
        } else {
            translator.translate(
                text,
                TranslateLanguage.ENGLISH,
                TranslateLanguage.KOREAN
            )
        }
    }

    override suspend fun generateStory(
        title: String,
        isKorean: Boolean
    ): String {

        // 더미 생성 로직
        val base = if (isKorean)
            "그날의 기억은 아직도 생생하다..."
        else
            "The memory of that day still lingers..."

        return buildString {
            append(title).append("\n\n")
            repeat(20) {
                append(base).append("\n\n")
            }
        }
    }
}
