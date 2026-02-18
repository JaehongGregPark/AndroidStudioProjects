package com.example.audioscript.viewmodel

import android.content.Context
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader
import java.util.Locale

class MainViewModel : ViewModel() {

    private val _text = MutableStateFlow("")

    val text: StateFlow<String> = _text

    // ✅ 여기 추가
    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> = _fileName

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText
    private var tts: TextToSpeech? = null

    fun initTTS(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context) {
                tts?.language = Locale.KOREAN
            }
        }
    }

    fun speak(context: Context) {
        initTTS(context)
        tts?.speak(_text.value, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // ✅ TXT 샘플 생성 (외부 앱 전용 폴더)
    fun createSampleTxt(context: Context) {
        viewModelScope.launch {
            try {
                val content = """
                Sample TXT File
                
                Hello World!
                안녕하세요.
            """.trimIndent()

                val resolver = context.contentResolver

                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "sample.txt")
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
                }

                val uri = resolver.insert(
                    android.provider.MediaStore.Files.getContentUri("external"),
                    values
                )

                uri?.let {
                    resolver.openOutputStream(it)?.use { output ->
                        output.write(content.toByteArray())
                    }
                    _text.value = "Download 폴더에 sample.txt 생성 완료"
                }

            } catch (e: Exception) {
                _text.value = "TXT 생성 오류: ${e.message}"
            }
        }
    }


    // ✅ PDF 샘플 생성
    fun createSamplePdf(context: Context) {
        viewModelScope.launch {
            try {
                val file = File(
                    context.getExternalFilesDir(null),
                    "sample.pdf"
                )

                withContext(Dispatchers.IO) {
                    val document = PDDocument()
                    val page = PDPage()
                    document.addPage(page)

                    val contentStream = PDPageContentStream(document, page)
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA, 14f)
                    contentStream.newLineAtOffset(50f, 700f)
                    contentStream.showText("Sample PDF File")
                    contentStream.newLineAtOffset(0f, -20f)
                    contentStream.showText("Hello from generated PDF.")
                    contentStream.endText()
                    contentStream.close()

                    document.save(file)
                    document.close()
                }

                _text.value = "PDF 생성 완료\n\n저장 위치:\n${file.absolutePath}"

                Log.d("FILE", "PDF saved at: ${file.absolutePath}")

            } catch (e: Exception) {
                _text.value = "PDF 생성 오류: ${e.message}"
            }
        }
    }

    // ✅ 파일 선택 후 읽기
    fun loadFromUri(context: Context, uri: Uri) {

        viewModelScope.launch {

            try {

                val resolver = context.contentResolver
                val nameCursor = resolver.query(uri, null, null, null, null)

                nameCursor?.use {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (it.moveToFirst()) {
                        _fileName.value = it.getString(nameIndex)
                    }
                }

                val mime = resolver.getType(uri)

                val text = if (mime == "application/pdf") {

                    withContext(Dispatchers.IO) {
                        val input = resolver.openInputStream(uri)
                        val document = PDDocument.load(input)
                        val stripper = PDFTextStripper()
                        val result = stripper.getText(document)
                        document.close()
                        input?.close()
                        result
                    }

                } else {

                    val input = resolver.openInputStream(uri)
                    val result = input?.bufferedReader()?.readText()
                    input?.close()
                    result ?: ""
                }

                _text.value = text
                speak(context) // 🔥 자동 TTS 실행

            } catch (e: Exception) {
                _text.value = "파일 읽기 오류: ${e.message}"
            }
        }
    }
    private fun generateLongStory(title: String, isKorean: Boolean): String {

        val paragraph = if (isKorean) {
            """
        그날의 기억은 아직도 선명하다. 바람은 조용히 불었고,
        사람들의 발걸음은 느렸다. 나는 작은 결심을 했고,
        그것은 생각보다 큰 변화를 가져왔다.
        시간이 지나도 변하지 않는 것은 마음 깊은 곳의 진심이었다.
        우리는 각자의 자리에서 조용히 꿈을 꾸고 있었다.
        """.trimIndent()
        } else {
            """
        The memory of that day still lingers.
        The wind moved gently through the streets,
        and people walked with quiet determination.
        A small decision led to an unexpected change.
        Some truths remain untouched by time.
        """.trimIndent()
        }

        val builder = StringBuilder()
        builder.append(title).append("\n\n")

        repeat(40) {   // 40 paragraphs ≈ 5분 분량
            builder.append(paragraph).append("\n\n")
        }

        return builder.toString()
    }

    private fun getFiveMinuteStories(): List<Triple<String, String, Boolean>> {

        return listOf(

            // 한국 5개
            Triple("korea_story_1", generateLongStory("고요한 바다", true), true),
            Triple("korea_story_2", generateLongStory("봄의 끝에서", true), true),
            Triple("korea_story_3", generateLongStory("오래된 편지", true), true),
            Triple("korea_story_4", generateLongStory("회색 도시", true), true),
            Triple("korea_story_5", generateLongStory("기다림의 의미", true), true),

            // 미국 5개
            Triple("usa_story_1", generateLongStory("The Silent Harbor", false), false),
            Triple("usa_story_2", generateLongStory("Before the Sunrise", false), false),
            Triple("usa_story_3", generateLongStory("Letters Never Sent", false), false),
            Triple("usa_story_4", generateLongStory("Shadows in the City", false), false),
            Triple("usa_story_5", generateLongStory("The Meaning of Waiting", false), false)
        )
    }
    fun createFiveMinuteSamples(context: Context) {

        viewModelScope.launch {

            val resolver = context.contentResolver
            val stories = getFiveMinuteStories()

            stories.forEach { (fileBaseName, content, _) ->

                try {

                    // ---------------- TXT ----------------
                    val txtValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "$fileBaseName.txt")
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/StorySamples/")
                    }

                    val txtUri = resolver.insert(
                        android.provider.MediaStore.Files.getContentUri("external"),
                        txtValues
                    )

                    txtUri?.let {
                        resolver.openOutputStream(it)?.use { output ->
                            output.write(content.toByteArray())
                        }
                    }

                    // ---------------- PDF ----------------
                    val pdfValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "$fileBaseName.pdf")
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download/StorySamples/")
                    }

                    val pdfUri = resolver.insert(
                        android.provider.MediaStore.Files.getContentUri("external"),
                        pdfValues
                    )

                    pdfUri?.let {

                        resolver.openOutputStream(it)?.use { output ->

                            val document = PDDocument()
                            val page = PDPage()
                            document.addPage(page)

                            val contentStream = PDPageContentStream(document, page)
                            contentStream.beginText()
                            contentStream.setFont(PDType1Font.HELVETICA, 10f)
                            contentStream.newLineAtOffset(40f, 750f)

                            var lineY = 750f

                            content.lines().take(45).forEach { line ->

                                if (lineY < 50f) return@forEach

                                contentStream.showText(line.take(90))
                                contentStream.newLineAtOffset(0f, -15f)
                                lineY -= 15f
                            }

                            contentStream.endText()
                            contentStream.close()

                            document.save(output)
                            document.close()
                        }
                    }

                } catch (e: Exception) {
                    Log.e("PDF_ERROR", "Error creating $fileBaseName", e)
                }
            }

            _text.value = "10개 작품 (TXT+PDF) 생성 완료"
        }
    }

    fun checkStorySamples(context: Context) {

        viewModelScope.launch {

            val resolver = context.contentResolver
            val uri = android.provider.MediaStore.Files.getContentUri("external")

            val projection = arrayOf(
                android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
                android.provider.MediaStore.MediaColumns.RELATIVE_PATH
            )

            val selection = "${android.provider.MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("%Download/StorySamples/%")

            val cursor = resolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )

            val builder = StringBuilder()

            cursor?.use {
                while (it.moveToNext()) {

                    val name = it.getString(0)
                    val path = it.getString(1)

                    builder.append("파일: $name\n경로: $path\n\n")
                }
            }

            _text.value = if (builder.isEmpty()) {
                "StorySamples 폴더에 파일이 없습니다."
            } else {
                builder.toString()
            }
        }
    }


    fun translateText(context: Context) {

        viewModelScope.launch {

            val original = _text.value

            val translated = if (original.contains(Regex("[가-힣]"))) {
                "Translated to English:\n\n$original"
            } else {
                "한국어 번역:\n\n$original"
            }

            _translatedText.value = translated
            _text.value = translated

            speak(context)
        }
    }


    override fun onCleared() {
        tts?.shutdown()
        super.onCleared()
    }
}
