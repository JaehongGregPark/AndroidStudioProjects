package com.example.scriptaudio.reader

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

//import org.apache.pdfbox.pdmodel.PDDocument
//import org.apache.pdfbox.text.PDFTextStripper

/**
 * PdfReaderEngine
 *
 * 대용량 PDF Reader
 *
 * 특징
 * 1000페이지 이상 대응
 * 페이지 단위 읽기
 */
object PdfReaderEngine {

    fun readPdfPages(
        context: Context,
        uri: Uri
    ): List<String> {

        val pages = mutableListOf<String>()

        val inputStream =
            context.contentResolver.openInputStream(uri)

        val document = PDDocument.load(inputStream)

        val stripper = PDFTextStripper()

        val totalPages = document.numberOfPages

        for (page in 1..totalPages) {

            stripper.startPage = page
            stripper.endPage = page

            val text = stripper.getText(document)

            pages.add(text)

        }

        document.close()

        return pages
    }

}