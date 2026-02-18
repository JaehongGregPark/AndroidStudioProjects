package com.example.audioscript.domain.usecase

import com.example.audioscript.domain.repository.TextRepository
import javax.inject.Inject

class LoadTextUseCase @Inject constructor(
    private val repository: TextRepository
) {
    suspend operator fun invoke(): String =
        repository.loadText()
}
