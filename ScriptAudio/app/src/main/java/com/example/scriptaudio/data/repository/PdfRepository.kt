package com.example.scriptaudio.data.repository

/**
 * PdfRepository
 *
 * PDF 데이터 처리 계층
 */

import java.io.File

import com.example.scriptaudio.engine.pdf.PdfReaderEngine

class PdfRepository(

    private val pdfReaderEngine: PdfReaderEngine = PdfReaderEngine()

) {

    /**
     * PDF 전체 텍스트 로딩
     */
    fun loadText(

        file: File

    ): String {

        return try {

            pdfReaderEngine.extractText(file)

        } catch (e: Exception) {

            e.printStackTrace()

            ""

        }

    }

}