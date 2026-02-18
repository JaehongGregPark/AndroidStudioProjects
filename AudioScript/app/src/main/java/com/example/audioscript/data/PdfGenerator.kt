package com.example.audioscript.data.pdf

import android.content.Context
import android.os.Environment
import com.tom_roush.pdfbox.pdmodel.*
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PDF 생성기
 * - NanumGothic 폰트 사용 (한글 지원)
 */
@Singleton
class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createPdf(text: String, fileName: String): File {

        val document = PDDocument()
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        val fontStream = context.assets.open("fonts/NanumGothic.ttf")
        val font = PDType0Font.load(document, fontStream)

        val contentStream = PDPageContentStream(document, page)

        contentStream.beginText()
        contentStream.setFont(font, 12f)
        contentStream.newLineAtOffset(40f, 750f)

        text.lines().forEach {
            contentStream.showText(it)
            contentStream.newLineAtOffset(0f, -15f)
        }

        contentStream.endText()
        contentStream.close()

        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "$fileName.pdf"
        )

        document.save(file)
        document.close()

        return file
    }
}
