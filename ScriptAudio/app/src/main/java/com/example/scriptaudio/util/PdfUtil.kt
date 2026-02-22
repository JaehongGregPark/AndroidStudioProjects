package com.example.scriptaudio.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import java.io.File

object PdfUtil {

    fun savePdf(

        context: Context,

        text: String

    ) {

        val pdf = PdfDocument()

        val page = pdf.startPage(

            PdfDocument.PageInfo.Builder(

                300,

                600,

                1

            ).create()

        )

        page.canvas.drawText(

            text,

            10f,

            25f,

            android.graphics.Paint()

        )

        pdf.finishPage(page)

        val file = File(

            context.getExternalFilesDir(null),

            "script.pdf"

        )

        pdf.writeTo(

            file.outputStream()

        )

        pdf.close()

    }

}