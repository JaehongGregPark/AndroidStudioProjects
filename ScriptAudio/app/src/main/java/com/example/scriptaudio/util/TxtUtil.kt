package com.example.scriptaudio.util

import android.content.Context
import java.io.File

/**
 * TXT 저장 유틸
 *
 */
object TxtUtil {

    fun save(

        context: Context,

        text: String

    ) {

        File(

            context.filesDir,

            "script.txt"

        ).writeText(text)

    }

}