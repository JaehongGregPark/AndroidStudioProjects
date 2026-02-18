package com.example.audioscript.domain.usecase

import com.example.audioscript.domain.repository.TextRepository
import javax.inject.Inject

/**
 * TXT 저장 유즈케이스
 */
class ExportTxtUseCase @Inject constructor(

    private val repository: TextRepository

) {

    suspend operator fun invoke(

        fileName: String,
        content: String

    ) {

        repository.exportTxt(
            fileName,
            content
        )

    }

}
