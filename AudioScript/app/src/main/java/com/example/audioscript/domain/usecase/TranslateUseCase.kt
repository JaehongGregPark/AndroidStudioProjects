package com.example.audioscript.domain.usecase

import com.example.audioscript.domain.repository.TextRepository
import javax.inject.Inject

/**
 * 번역 유즈케이스
 *
 * - 추후 OpenAI API 등 외부 API 연결 시
 * - 이 레이어만 수정하면 됨
 */
class TranslateUseCase @Inject constructor(
    private val repository: TextRepository
) {
    suspend operator fun invoke(text: String): String {
        return repository.translate(text)
    }
}
