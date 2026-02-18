package com.example.audioscript.domain.usecase

import com.example.audioscript.domain.repository.TextRepository
import javax.inject.Inject

/**
 * 소설 생성 유즈케이스
 */
class GenerateStoryUseCase @Inject constructor(
    private val repository: TextRepository
) {
    suspend operator fun invoke(
        title: String,
        isKorean: Boolean
    ): String {
        return repository.generateStory(title, isKorean)
    }
}
