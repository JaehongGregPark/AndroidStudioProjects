package com.example.pythonttsmvvmapp.reader.data.datasource

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.pythonttsmvvmapp.data.parser.PdfParser
import com.example.pythonttsmvvmapp.data.parser.TxtParser
import javax.inject.Inject

/**
 * 파일에서 텍스트를 추출하는 진짜 작업 담당
 */
class FileDataSource @Inject constructor(
    private val txtParser: TxtParser,
    private val pdfParser: PdfParser
) {

    suspend fun parse(
        context: Context,
        uri: Uri
    ): Pair<String, String> {

        val name = getFileName(context, uri)

        val text = when {
            name.endsWith(".txt", true) -> txtParser.parse(uri)
            name.endsWith(".pdf", true) -> pdfParser.parse(uri)
            else -> "지원하지 않는 파일 형식입니다."
        }

        return name to text
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) {
                name = it.getString(index)
            }
        }
        return name
    }
}
