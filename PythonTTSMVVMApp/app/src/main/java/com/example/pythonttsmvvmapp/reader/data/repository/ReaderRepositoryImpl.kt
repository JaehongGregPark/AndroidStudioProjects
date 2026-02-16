package com.example.pythonttsmvvmapp.reader.data.repository

import android.content.Context
import android.net.Uri
import com.example.pythonttsmvvmapp.reader.data.datasource.FileDataSource
import com.example.pythonttsmvvmapp.reader.data.datasource.PreferenceDataSource
import javax.inject.Inject

class ReaderRepositoryImpl @Inject constructor(
    private val fileDataSource: FileDataSource,
    private val prefDataSource: PreferenceDataSource
) : ReaderRepository {

    override suspend fun parseFile(
        context: Context,
        uri: Uri
    ) = fileDataSource.parse(context, uri)

    override fun saveLastPosition(
        context: Context,
        uri: String,
        start: Int,
        end: Int
    ) = prefDataSource.saveLastPosition(context, uri, start, end)

    override fun restoreLastPosition(
        context: Context,
        uri: String
    ) = prefDataSource.restoreLastPosition(context, uri)
}
