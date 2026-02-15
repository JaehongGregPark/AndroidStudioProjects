package com.example.pythonttsmvvmapp.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

/**
 * Uri 로부터 TXT / PDF 파일을 읽어서
 * 문자열로 변환하는 유틸
 */
object FileReader {

    fun readText(context: Context, uri: Uri): String {

        val type = context.contentResolver.getType(uri)

        return when {
            type?.contains("text") == true -> readTxt(context, uri)
            type?.contains("pdf") == true -> readPdf(context, uri)
            else -> ""
        }
    }

    /** TXT 읽기 */
    private fun readTxt(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() } ?: ""
    }

    /** PDF 읽기 */
    private fun readPdf(context: Context, uri: Uri): String {

        context.contentResolver.openInputStream(uri)?.use { input ->
            val document = PDDocument.load(input)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            return text
        }

        return ""
    }
}
