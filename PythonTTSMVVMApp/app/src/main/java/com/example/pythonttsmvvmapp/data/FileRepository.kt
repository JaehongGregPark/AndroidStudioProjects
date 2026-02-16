package com.example.pythonttsmvvmapp.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

/**
 * 파일 처리 담당 클래스
 *
 * ✔ 파일 이름 얻기
 * ✔ TXT 읽기
 * ✔ PDF 읽기
 * ✔ 확장자로 자동 분기
 * ✔ 최근 파일 저장 / 불러오기
 */
class FileRepository {

    /**
     * Uri 로부터 파일 이름 얻기
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown"

        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) {
                name = it.getString(index)
            }
        }

        return name
    }

    /**
     * TXT 파일 읽기
     */
    private fun readTxt(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.bufferedReader().use {
            it?.readText() ?: ""
        }
    }

    /**
     * PDF 파일 읽기
     */
    private fun readPdf(context: Context, uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri) ?: return ""

        val document = PDDocument.load(input)
        val stripper = PDFTextStripper()
        val text = stripper.getText(document)

        document.close()

        return text
    }

    /**
     * 파일 이름으로 형식 판별 후 자동 읽기
     */
    fun readFile(context: Context, uri: Uri, fileName: String): String {
        return when {
            fileName.endsWith(".txt", true) -> readTxt(context, uri)
            fileName.endsWith(".pdf", true) -> readPdf(context, uri)
            else -> "지원하지 않는 파일 형식입니다."
        }
    }

    // ----------------------------------------------------
    // 최근 파일 저장
    // ----------------------------------------------------

    fun saveRecent(context: Context, file: RecentFile) {
        val pref = context.getSharedPreferences("recent", Context.MODE_PRIVATE)

        val old = pref.getStringSet("list", mutableSetOf())!!.toMutableSet()

        // name|uri 형태로 저장
        old.add("${file.name}|${file.uri}")

        pref.edit().putStringSet("list", old).apply()
    }

    // ----------------------------------------------------
    // 최근 파일 불러오기
    // ----------------------------------------------------

    fun loadRecent(context: Context): List<RecentFile> {
        val pref = context.getSharedPreferences("recent", Context.MODE_PRIVATE)
        val set = pref.getStringSet("list", emptySet()) ?: emptySet()

        return set.mapNotNull {
            val sp = it.split("|")
            if (sp.size == 2) {
                RecentFile(sp[0], sp[1])
            } else null
        }
    }
}
