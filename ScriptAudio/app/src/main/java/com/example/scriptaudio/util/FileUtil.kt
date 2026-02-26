package com.example.scriptaudio.util

import android.content.Context
import java.io.File

object FileUtil {

    private const val FOLDER_NAME = "ScriptAudio"


    /**
     * 폴더 반환
     */
    private fun getDir(context: Context): File {

        val dir = File(
            context.getExternalFilesDir(null),
            FOLDER_NAME
        )

        if (!dir.exists())
            dir.mkdirs()

        return dir
    }


    fun createTxtFile(
        context: Context,
        fileName: String
    ): File {

        return File(
            getDir(context),
            "$fileName.txt"
        )
    }


    fun createPdfFile(
        context: Context,
        fileName: String
    ): File {

        return File(
            getDir(context),
            "$fileName.pdf"
        )
    }



    /**
     * 파일 목록 반환
     */
    fun getFileList(
        context: Context
    ): List<File> {

        return getDir(context)
            .listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    }



    /**
     * 파일 삭제
     */
    fun delete(
        file: File
    ) {

        if (file.exists())
            file.delete()

    }

}