package com.example.scriptaudio.ui.reader.components

/**
 * PdfPageRenderer
 *
 * PDF 페이지를 Bitmap으로 렌더링
 */

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor

import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap

import java.io.File

@Composable
fun PdfPageRenderer(

    file: File,

    pageIndex: Int

) {

    var bitmap by remember {

        mutableStateOf<Bitmap?>(null)

    }

    LaunchedEffect(file, pageIndex) {

        val fd = ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )

        val renderer = PdfRenderer(fd)

        val page = renderer.openPage(pageIndex)

        val bmp = Bitmap.createBitmap(

            page.width,
            page.height,
            Bitmap.Config.ARGB_8888

        )

        page.render(

            bmp,
            null,
            null,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY

        )

        bitmap = bmp

        page.close()
        renderer.close()
    }

    bitmap?.let {

        Image(

            bitmap = it.asImageBitmap(),

            contentDescription = "PDF Page",

            modifier = Modifier.fillMaxWidth()

        )

    }

}