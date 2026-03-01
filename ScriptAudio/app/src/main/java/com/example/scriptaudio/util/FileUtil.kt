package com.example.scriptaudio.util

import android.content.Context
import android.os.Environment
import java.io.File

object FileUtil {

    private const val FOLDER_NAME = "ScriptAudio"

    /**
     * 🔥 Documents/ScriptAudio 폴더 반환
     *
     * 저장 위치:
     * /storage/emulated/0/Documents/ScriptAudio
     *
     * ✔ 사용자 파일앱에서 바로 보임
     * ✔ PC 연결 시 바로 접근 가능
     */
    private fun getDir(): File {

        val documentsDir =
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            )

        val dir = File(documentsDir, FOLDER_NAME)

        if (!dir.exists()) {
            dir.mkdirs()
        }

        return dir
    }

    /**
     * TXT 파일 생성
     */
    fun createTxtFile(
        context: Context,
        fileName: String
    ): File {

        return File(
            getDir(),
            "$fileName.txt"
        )
    }

    /**
     * PDF 파일 생성
     */
    fun createPdfFile(
        context: Context,
        fileName: String
    ): File {

        return File(
            getDir(),
            "$fileName.pdf"
        )
    }

    /**
     * 파일 목록 반환
     */
    fun getFileList(
        context: Context
    ): List<File> {

        return getDir()
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
        if (file.exists()) {
            file.delete()
        }
    }
}