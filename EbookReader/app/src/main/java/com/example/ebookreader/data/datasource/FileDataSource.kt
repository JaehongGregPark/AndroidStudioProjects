package com.example.ebookreader.data.datasource

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.ebookreader.data.parser.PdfParser
import com.example.ebookreader.data.parser.TxtParser
import com.example.ebookreader.domain.model.ParsedFile
import javax.inject.Inject

/**
 * Android API 를 직접 사용하는 최하위 계층
 *
 * ✔ Uri → 파일명 추출
 * ✔ 확장자 기반 Parser 선택
 * ✔ 텍스트 변환
 */
class FileDataSource @Inject constructor(
    private val txtParser: TxtParser,
    private val pdfParser: PdfParser
) {

    suspend fun parse(
        context: Context,
        uri: Uri
    ): ParsedFile {

        val name = extractFileName(context, uri)

        val text = when {
            name.endsWith(".txt", true) -> txtParser.parse(uri)
            name.endsWith(".pdf", true) -> pdfParser.parse(uri)
            else -> ""
        }

        return ParsedFile(name, text)
    }

    private fun extractFileName(
        context: Context,
        uri: Uri
    ): String {
        var name = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) {
                name = it.getString(index)
            }
        }
        return name
    }
}
