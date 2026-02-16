package com.example.ebookreader.data.parser

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * TXT 파일 파서
 */
class TxtParser @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentParser {

    override suspend fun parse(uri: Uri): String =
        withContext(Dispatchers.IO) {
            context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() } ?: ""
        }
}
