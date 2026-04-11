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
    fun write(
        file: File,
        content: String
    ) {

        file.writeText(content)

    }

    /**
     * txt 파일 읽기
     */
    fun read(
        file: File
    ): String {

        return file.readText()

    }

}