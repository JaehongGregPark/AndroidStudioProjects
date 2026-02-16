package com.example.ebookreader.domain.usecase

import com.example.ebookreader.domain.repository.ReaderRepository
import javax.inject.Inject

/**
 * 이어 읽기 위치 저장/복원 유스케이스
 */
class ReadingPositionUseCase @Inject constructor(
    private val repository: ReaderRepository
) {

    fun save(uri: String, start: Int, end: Int) =
        repository.saveReadingPosition(uri, start, end)

    fun restore(uri: String) =
        repository.restoreReadingPosition(uri)
}
