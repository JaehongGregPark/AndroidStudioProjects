package com.example.audioscript.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import android.content.ContentResolver
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File

import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text

    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ✅ TTS 지연 초기화
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private fun initTTS() {
        if (tts == null) {
            tts = TextToSpeech(getApplication()) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.KOREAN
                    isTtsReady = true
                }
            }
        }
    }

    fun updateText(newText: String) {
        _text.value = newText
    }

    fun speak(textToSpeak: String) {
        if (textToSpeak.isBlank()) return
        initTTS()
        if (isTtsReady) {
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun speakOriginal() = speak(_text.value)
    fun speakTranslated() = speak(_translatedText.value)

    // ✅ 번역은 IO 스레드에서 실행
    fun translateText() {
        val inputText = _text.value
        if (inputText.isBlank()) return

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val languageIdentifier = LanguageIdentification.getClient()
                val detectedLanguage =
                    languageIdentifier.identifyLanguage(inputText).await()

                if (detectedLanguage == "und") {
                    _translatedText.value = "언어를 감지할 수 없습니다."
                    return@launch
                }

                val sourceLang = when (detectedLanguage) {
                    "ko" -> TranslateLanguage.KOREAN
                    "en" -> TranslateLanguage.ENGLISH
                    else -> detectedLanguage
                }

                val targetLang = when (sourceLang) {
                    TranslateLanguage.KOREAN -> TranslateLanguage.ENGLISH
                    TranslateLanguage.ENGLISH -> TranslateLanguage.KOREAN
                    else -> TranslateLanguage.ENGLISH
                }

                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLang)
                    .setTargetLanguage(targetLang)
                    .build()

                val translator = Translation.getClient(options)

                val result = withContext(Dispatchers.IO) {
                    translator.downloadModelIfNeeded().await()
                    translator.translate(inputText).await()
                }

                _translatedText.value = result
                translator.close()

            } catch (e: Exception) {
                _translatedText.value = "번역 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTextFromFile(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val mimeType = contentResolver.getType(uri)

                if (mimeType == "application/pdf") {

                    // ✅ PDF 처리 (IO 스레드에서 실행)
                    val text = withContext(Dispatchers.IO) {
                        val inputStream = contentResolver.openInputStream(uri)
                        val document = PDDocument.load(inputStream)
                        val stripper = PDFTextStripper()
                        val extractedText = stripper.getText(document)
                        document.close()
                        inputStream?.close()
                        extractedText
                    }

                    _text.value = text

                } else {
                    // ✅ TXT 처리
                    val inputStream = contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val textBuilder = StringBuilder()
                    reader.forEachLine {
                        textBuilder.append(it).append("\n")
                    }

                    reader.close()
                    _text.value = textBuilder.toString()
                }

            } catch (e: Exception) {
                _text.value = "파일 읽기 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSampleFileAndLoad(context: android.content.Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val sampleText = """
                📄 Sample PDF/TXT File
                
                Hello, this is a sample file.
                안녕하세요. 이것은 샘플 파일입니다.
                
                This app can:
                - Load TXT files
                - Extract text from PDF
                - Translate text
                - Read text with TTS
                
                Enjoy testing 🚀
            """.trimIndent()

                val file = File(context.filesDir, "sample.txt")
                file.writeText(sampleText)

                _text.value = file.readText()

            } catch (e: Exception) {
                _text.value = "샘플 파일 생성 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSamplePdfAndLoad(context: android.content.Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val file = File(context.filesDir, "sample.pdf")

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
                    contentStream.showText("Hello, this is a generated PDF.")
                    contentStream.newLineAtOffset(0f, -20f)
                    contentStream.showText("안녕하세요. PDF 자동 생성 테스트입니다.")
                    contentStream.newLineAtOffset(0f, -20f)
                    contentStream.showText("PDF text extraction works!")

                    contentStream.endText()
                    contentStream.close()

                    document.save(file)
                    document.close()
                }

                // 생성 후 바로 읽기
                loadTextFromFile(context.contentResolver, android.net.Uri.fromFile(file))

            } catch (e: Exception) {
                _text.value = "샘플 PDF 생성 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
