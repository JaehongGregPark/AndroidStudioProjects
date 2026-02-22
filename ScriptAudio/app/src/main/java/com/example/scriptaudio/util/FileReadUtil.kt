package com.example.scriptaudio.util

import android.net.Uri
import android.content.Context

object FileReadUtil {

    fun readText(

        context: Context,
        uri: Uri

    ): String {

        return context.contentResolver.openInputStream(uri)

            ?.bufferedReader()

            ?.use { it.readText() }

            ?: ""

    }

}