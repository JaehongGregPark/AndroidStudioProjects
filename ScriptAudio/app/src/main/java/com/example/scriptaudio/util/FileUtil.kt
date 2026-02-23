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
    /**
     *
     * txt 파일 생성
     *
     * @param context
     * @param fileName 확장자 제외 이름
     *
     * 예:
     *
     * fileName = test
     *
     * 생성:
     *
     * test.txt
     *
     */
    fun createTxtFile(
        context: Context,
        fileName: String
    ): File {


        /**
         * 저장 폴더
         */
        val dir = File(

            context.getExternalFilesDir(null),

            "ScriptAudio"

        )


        /**
         * 폴더 없으면 생성
         */
        if (!dir.exists()) {

            dir.mkdirs()

        }


        /**
         * 파일 생성
         */
        return File(

            dir,

            "$fileName.txt"

        )

    }



    /**
     *
     * pdf 파일 생성
     *
     */
    fun createPdfFile(
        context: Context,
        fileName: String
    ): File {


        val dir = File(

            context.getExternalFilesDir(null),

            "ScriptAudio"

        )


        if (!dir.exists()) {

            dir.mkdirs()

        }


        return File(

            dir,

            "$fileName.pdf"

        )

    }


}