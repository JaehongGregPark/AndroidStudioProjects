package com.questionbank.android.parser

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.text.HtmlCompat
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream
import java.nio.charset.Charset
import java.util.Locale
import java.util.zip.ZipInputStream

class DocumentTextExtractor(private val context: Context) {

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun resolveFileName(uri: Uri): String {
        val resolver = context.contentResolver
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex) ?: "imported-document"
            }
        }
        return uri.lastPathSegment ?: "imported-document"
    }

    fun extract(uri: Uri): String {
        val fileName = resolveFileName(uri)
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.getDefault())

        return when (extension) {
            "txt" -> readTextFile(uri)
            "docx" -> readDocx(uri)
            "pdf" -> readPdf(uri)
            "doc" -> throw IllegalArgumentException("현재는 docx 형식만 지원합니다. 기존 doc 파일은 docx로 변환해 주세요.")
            else -> throw IllegalArgumentException("지원하지 않는 파일 형식입니다: .$extension")
        }.normalizeDocumentText()
    }

    private fun readTextFile(uri: Uri): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("텍스트 파일을 열 수 없습니다.")
        return decodeBytes(bytes)
    }

    private fun readDocx(uri: Uri): String {
        val xmlText = context.contentResolver.openInputStream(uri)?.use { input ->
            extractDocumentXml(input)
        } ?: throw IllegalArgumentException("Word 파일을 열 수 없습니다.")

        return xmlText
            .replace("</w:p>", "\n")
            .replace("<w:tab/>", "\t")
            .replace(Regex("<[^>]+>"), " ")
            .let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString() }
    }

    private fun extractDocumentXml(input: InputStream): String {
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    return zip.readBytes().toString(Charsets.UTF_8)
                }
                entry = zip.nextEntry
            }
        }
        throw IllegalArgumentException("docx 내부에서 본문을 찾지 못했습니다.")
    }

    private fun readPdf(uri: Uri): String {
        context.contentResolver.openInputStream(uri)?.use { input ->
            PDDocument.load(input).use { document ->
                return PDFTextStripper().getText(document)
            }
        }
        throw IllegalArgumentException("PDF 파일을 열 수 없습니다.")
    }

    private fun decodeBytes(bytes: ByteArray): String {
        if (bytes.startsWith(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))) {
            return bytes.copyOfRange(3, bytes.size).toString(Charsets.UTF_8)
        }

        return decodeOrNull(bytes, Charsets.UTF_8)
            ?: decodeOrNull(bytes, Charset.forName("MS949"))
            ?: decodeOrNull(bytes, Charsets.ISO_8859_1)
            ?: String(bytes)
    }

    private fun decodeOrNull(bytes: ByteArray, charset: Charset): String? {
        return runCatching {
            val text = bytes.toString(charset)
            if (text.contains('\uFFFD')) null else text
        }.getOrNull()
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (size < prefix.size) return false
        return prefix.indices.all { index -> this[index] == prefix[index] }
    }

    private fun String.normalizeDocumentText(): String {
        return lineSequence()
            .map { it.replace('\u00A0', ' ').trimEnd() }
            .joinToString("\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}
