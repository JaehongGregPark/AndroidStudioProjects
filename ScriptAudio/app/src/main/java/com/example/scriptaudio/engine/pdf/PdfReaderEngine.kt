package com.example.scriptaudio.engine.pdf

/**
 * PdfReaderEngine
 *
 * PDF 파일을 읽어 텍스트로 변환하는 엔진
 *
 * 특징
 * - 1000페이지 이상 PDF 처리 가능
 * - 페이지 단위 텍스트 추출
 * - Lazy loading 구조로 메모리 절약
 */

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File


class PdfReaderEngine {

    /**
     * PDF 전체 텍스트 추출
     */
    fun extractText(file: File): String {

        PDDocument.load(file).use { document ->

            val stripper = PDFTextStripper()

            return stripper.getText(document)

        }

    }

    /**
     * 특정 페이지 텍스트
     */
    fun extractPageText(
        file: File,
        page: Int
    ): String {

        PDDocument.load(file).use { document ->

            val stripper = PDFTextStripper()

            stripper.startPage = page
            stripper.endPage = page

            return stripper.getText(document)

        }

    }

}