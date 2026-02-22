package com.example.scriptaudio.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

/**
 * 파일 저장 / 읽기 담당 클래스
 */
object FileUtil {

    /**
     * TXT 저장
     */
    fun saveTxt(

        context: Context,
        fileName: String,
        content: String

    ): String {

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        val file = File(dir, "$fileName.txt")

        FileOutputStream(file).use {

            it.write(content.toByteArray())

        }

        return file.absolutePath
    }

}