package com.example.audioscript.domain.usecase

import android.net.Uri
import com.example.audioscript.domain.repository.TextRepository
import javax.inject.Inject

/**
 * 파일 로딩 유즈케이스
 *
 * ViewModel은 Repository에 직접 접근하지 않고
 * 반드시 UseCase를 통해 접근한다.
 */
class LoadTextUseCase @Inject constructor(
    private val repository: TextRepository
) {
    suspend operator fun invoke(uri: Uri): String {
        return repository.loadText(uri)
    }
}
