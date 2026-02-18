package com.example.audioscript.data.repository

import android.net.Uri
import com.example.audioscript.data.datasource.LocalFileDataSource
import com.example.audioscript.domain.repository.TextRepository
import javax.inject.Inject

/**
 * Repository 구현체
 *
 * - Domain 인터페이스 구현
 * - 실제 데이터 접근은 DataSource에 위임
 */
class TextRepositoryImpl @Inject constructor(
    private val dataSource: LocalFileDataSource
) : TextRepository {

    override suspend fun loadText(uri: Uri): String {
        return dataSource.readText(uri)
    }

    override suspend fun translate(text: String): String {

        val isKorean = text.contains(Regex("[가-힣]"))

        return if (isKorean) {
            "English Translation:\n\n$text"
        } else {
            "한국어 번역:\n\n$text"
        }
    }

    override suspend fun generateStory(
        title: String,
        isKorean: Boolean
    ): String {

        val paragraph = if (isKorean) {
            "그날의 기억은 아직도 생생하다..."
        } else {
            "The memory of that day still lingers..."
        }

        return buildString {
            append(title).append("\n\n")
            repeat(40) {
                append(paragraph).append("\n\n")
            }
        }
    }
}
