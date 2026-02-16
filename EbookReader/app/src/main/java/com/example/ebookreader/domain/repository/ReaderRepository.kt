package com.example.ebookreader.domain.repository

import android.content.Context
import android.net.Uri
import com.example.ebookreader.domain.model.ParsedFile

/**
 * Reader 기능의 데이터 진입점 (Port)
 *
 * UseCase 는 이 인터페이스만 의존한다.
 * 실제 구현은 data 계층에서 제공된다.
 */
interface ReaderRepository {

    /**
     * 파일을 열고 텍스트로 변환
     */
    suspend fun openFile(
        context: Context,
        uri: Uri
    ): ParsedFile

    /**
     * 마지막 읽은 위치 저장
     */
    fun saveReadingPosition(
        uri: String,
        start: Int,
        end: Int
    )

    /**
     * 마지막 읽은 위치 복원
     */
    fun restoreReadingPosition(
        uri: String
    ): Pair<Int, Int>
}
