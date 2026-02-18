package com.example.audioscript.domain.usecase

import com.example.audioscript.domain.repository.TextRepository
import javax.inject.Inject

/**
 * ===============================
 * Domain Layer - UseCase
 * ===============================
 *
 * ✔ 하나의 유즈케이스는 하나의 비즈니스 기능만 담당
 * ✔ ViewModel은 Repository에 직접 접근하지 않음
 * ✔ 테스트가 쉬워짐
 *
 * 역할:
 *  - 번역 비즈니스 로직 수행
 */
class TranslateUseCase @Inject constructor(
    private val repository: TextRepository
) {

    /**
     * operator invoke 사용 이유:
     * translateUseCase(text) 형태로 호출 가능
     */
    suspend operator fun invoke(text: String): String {
        return repository.translate(text)
    }
}
