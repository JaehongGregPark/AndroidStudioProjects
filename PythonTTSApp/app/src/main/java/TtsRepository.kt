package com.example.pythonttsapp

import android.content.ContentResolver
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

/* =========================================================
   TtsRepository.kt
   =========================================================

   📌 데이터 처리 계층

   역할:
   - 파일에서 텍스트 읽기
   - PDF 텍스트 추출
   - 문장 / 문단 분리

   ViewModel은 "무엇을 할지"
   Repository는 "어떻게 할지"

========================================================= */

class TtsRepository {

    /* =====================================================
       TXT 파일 읽기
       ===================================================== */
    fun readText(resolver: ContentResolver, uri: Uri): String {

        // InputStream → BufferedReader → 전체 텍스트 읽기
        return resolver.openInputStream(uri)
            ?.bufferedReader()
            ?.readText()
            ?: ""
    }

    /* =====================================================
       PDF 파일 읽기
       ===================================================== */
    fun readPdf(resolver: ContentResolver, uri: Uri): String {

        resolver.openInputStream(uri).use { input ->

            // PDF 로드
            val document = PDDocument.load(input)

            // 텍스트 추출기
            val stripper = PDFTextStripper()

            val text = stripper.getText(document)

            document.close()

            return text
        }
    }

    /* =====================================================
       PDF 여부 판단
       ===================================================== */
    fun isPdf(resolver: ContentResolver, uri: Uri): Boolean {

        val mime = resolver.getType(uri)

        if (mime == "application/pdf") return true

        return uri.lastPathSegment
            ?.lowercase()
            ?.endsWith(".pdf") == true
    }

    /* =====================================================
       문장 분리
       ===================================================== */
    fun splitSentences(text: String): List<String> {

        // . ! ? 뒤 공백 기준 분리
        return text.split(Regex("(?<=[.!?])\\s+"))
    }

    /* =====================================================
       문단 분리
       ===================================================== */
    fun splitParagraphs(text: String): List<String> {

        // 빈 줄 기준 분리
        return text.split(Regex("\\n\\s*\\n"))
    }
}
