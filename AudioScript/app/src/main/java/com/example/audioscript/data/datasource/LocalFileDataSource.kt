package com.example.audioscript.data.datasource

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream
import javax.inject.Inject

/**
 * 실제 파일 입출력을 담당하는 DataSource
 *
 * - PDF
 * - TXT
 * - 기타 문서
 *
 * Android Framework 의존 가능
 */
class LocalFileDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Uri로부터 텍스트 읽기
     */
    fun readText(uri: Uri): String {

        val resolver = context.contentResolver
        val mime = resolver.getType(uri)

        return if (mime == "application/pdf") {

            val inputStream: InputStream? =
                resolver.openInputStream(uri)

            val document = PDDocument.load(inputStream)
            val text = PDFTextStripper().getText(document)

            document.close()
            inputStream?.close()

            text

        } else {

            resolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: ""
        }
    }
}
