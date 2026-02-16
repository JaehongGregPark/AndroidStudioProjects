package com.example.ebookreader.data.repository

import android.content.Context
import android.net.Uri
import com.example.ebookreader.data.datasource.FileDataSource
import com.example.ebookreader.data.datasource.PreferenceDataSource
import com.example.ebookreader.domain.repository.ReaderRepository
import javax.inject.Inject

/**
 * Domain 의 ReaderRepository 구현체
 *
 * 여러 DataSource 를 조합한다.
 */
class ReaderRepositoryImpl @Inject constructor(
    private val fileDataSource: FileDataSource,
    private val preferenceDataSource: PreferenceDataSource
) : ReaderRepository {

    override suspend fun openFile(
        context: Context,
        uri: Uri
    ) = fileDataSource.parse(context, uri)

    override fun saveReadingPosition(
        uri: String,
        start: Int,
        end: Int
    ) = preferenceDataSource.save(uri, start, end)

    override fun restoreReadingPosition(uri: String) =
        preferenceDataSource.restore(uri)
}
