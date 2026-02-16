package com.example.pythonttsmvvmapp.reader.usecase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.pythonttsmvvmapp.data.parser.PdfParser
import com.example.pythonttsmvvmapp.data.parser.TxtParser
import com.example.pythonttsmvvmapp.reader.data.repository.ReaderRepository
import javax.inject.Inject

/**
 * 파일을 열고
 * 이름 추출 + 파싱까지 담당
 */
/**
 *
 *     private val txtParser: TxtParser,
 *     private val pdfParser: PdfParser
 * ) {
 *
 *     suspend operator fun invoke(
 *         context: Context,
 *         uri: Uri
 *     ): Pair<String, String> {
 *
 *         val fileName = getFileName(context, uri)
 *
 *         val text = when {
 *             fileName.endsWith(".txt", true) -> txtParser.parse(uri)
 *             fileName.endsWith(".pdf", true) -> pdfParser.parse(uri)
 *             else -> "지원하지 않는 파일 형식입니다."
 *         }
 *
 *         return fileName to text
 *     }
 *
 */
class OpenFileUseCase @Inject constructor(
    private val repository: ReaderRepository
) {
    suspend operator fun invoke(
        context: Context,
        uri: Uri
    ) = repository.parseFile(context, uri)
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

