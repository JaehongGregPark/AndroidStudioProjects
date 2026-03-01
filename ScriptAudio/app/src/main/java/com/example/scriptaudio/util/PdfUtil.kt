package com.example.scriptaudio.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


/**
 *
 * PDF 유틸
 *
 */
object PdfUtil {



    /**
     * PDF 쓰기
     */
    fun write(
        file: File,
        content: String
    ) {

        val document = PdfDocument()


        val pageInfo =
            PdfDocument.PageInfo.Builder(
                300,
                600,
                1
            ).create()


        val page =
            document.startPage(pageInfo)


        val paint = Paint()

        paint.textSize = 12f


        page.canvas.drawText(

            content,

            10f,

            25f,

            paint

        )


        document.finishPage(page)


        document.writeTo(

            FileOutputStream(file)

        )


        document.close()

    }




    /**
     * PDF 읽기
     *
     * 샘플용
     */
    fun read(file: File): String {

        return try {

            PDDocument.load(file).use { document ->

                val stripper = PDFTextStripper()
                stripper.getText(document)

            }

        } catch (e: Exception) {

            "PDF 읽기 실패: ${e.message}"
        }
    }

    /**
     * 🔥 강화된 PDF 텍스트 추출
     *
     * ✔ 한글 깨짐 완화
     * ✔ 암호화 제거
     * ✔ 페이지 단위 읽기
     * ✔ fallback 처리
     */
    fun extractTextFromPdf(
        context: Context,
        contentResolver: ContentResolver,
        uri: Uri
    ): String {

        return try {

            PDFBoxResourceLoader.init(context)

            val inputStream: InputStream =
                contentResolver.openInputStream(uri)
                    ?: return "PDF 파일을 열 수 없습니다."

            val document = PDDocument.load(inputStream)

            if (document.isEncrypted) {
                document.setAllSecurityToBeRemoved(true)
            }

            val stripper = PDFTextStripper().apply {
                sortByPosition = true
                startPage = 1
                endPage = document.numberOfPages
            }

            val text = stripper.getText(document)

            document.close()
            inputStream.close()

            if (text.isBlank()) {
                "⚠ 텍스트를 추출할 수 없는 PDF입니다.\n(스캔본 또는 이미지 기반 PDF일 가능성 높음)"
            } else {
                text
            }

        } catch (e: Exception) {

            "PDF 읽기 실패:\n${e.message}"

        }
    }
}
