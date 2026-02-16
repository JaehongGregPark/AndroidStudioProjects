package com.example.ebookreader.data.parser

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * PDF 파일 파서
 *
 * ⚠ 실제 PDFBox 연동 위치
 */
class PdfParser @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentParser {

    override suspend fun parse(uri: Uri): String =
        withContext(Dispatchers.IO) {
            "PDF 파싱 라이브러리 연동 예정"
        }
}
