package com.example.scriptaudio.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
/**
 * PDF 저장 유틸
 *
 */
object PdfUtil {

    fun save(

        context: Context,

        text: String

    ) {

        val doc = PdfDocument()

        val page = doc.startPage(

            PdfDocument.PageInfo.Builder(

                300,
                600,
                1

            ).create()

        )


        /**
         * 텍스트 작성
         */
        page.canvas.drawText(

            text,
            10f,
            25f,
            android.graphics.Paint()

        )


        doc.finishPage(page)


        doc.writeTo(

            context.openFileOutput(

                "script.pdf",

                Context.MODE_PRIVATE

            )

        )

        doc.close()

    }

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


        page.canvas.drawText(

            content,

            10f,

            25f,

            android.graphics.Paint()

        )


        document.finishPage(page)


        document.writeTo(

            FileOutputStream(file)

        )


        document.close()


    }


}