package com.example.scriptaudio.util

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor

import java.io.File
import java.io.FileOutputStream


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
    fun read(
        file: File
    ): String {


        val fd =
            ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY
            )


        val renderer =
            PdfRenderer(fd)


        val page =
            renderer.openPage(0)


        val result =
            "PDF 파일 열림: ${file.name}"


        page.close()

        renderer.close()

        fd.close()


        return result

    }


}