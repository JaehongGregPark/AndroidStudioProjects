package com.example.pythonttsmvvmapp.reader.data.repository

import android.content.Context
import android.net.Uri

/**
 * 데이터 입출구
 *
 * UseCase 는 이것만 사용한다.
 */
interface ReaderRepository {

    suspend fun parseFile(
        context: Context,
        uri: Uri
    ): Pair<String, String>

    fun saveLastPosition(
        context: Context,
        uri: String,
        start: Int,
        end: Int
    )

    fun restoreLastPosition(
        context: Context,
        uri: String
    ): Pair<Int, Int>
}
