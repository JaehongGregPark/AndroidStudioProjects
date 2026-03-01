package com.example.scriptaudio.viewmodel

import android.app.Application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.scriptaudio.data.local.ScriptEntity
import com.example.scriptaudio.data.local.ScriptRepository
import com.example.scriptaudio.tts.TTSManager

import com.example.scriptaudio.util.FileUtil
import com.example.scriptaudio.util.TxtUtil
import com.example.scriptaudio.util.PdfUtil

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import android.content.ContentResolver
import android.net.Uri

import kotlinx.coroutines.withContext
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.tasks.await

/**
 *
 * MainViewModel
 *
 * HiltViewModel 에서는
 * getApplication() 사용하지 않고
 *
 * Application 을 직접 주입 받아야 함
 *
 */
@HiltViewModel
class MainViewModel @Inject constructor(

    /**
     * Application Context
     *
     * ✔ getApplication 대신 사용
     */
    private val application: Application,


    /**
     * Room Repository
     */
    private val repository: ScriptRepository,


    /**
     * TTS Manager
     */
    private val tts: TTSManager

) : ViewModel() {



    /**
     * 현재 스크립트 텍스트
     */
    private val _script = MutableStateFlow("")

    val script: StateFlow<String> = _script



    /**
     * TTS 속도 상태
     */
    private val _speechRate = MutableStateFlow(1f)

    val speechRate: StateFlow<Float> = _speechRate



    /**
     * TTS Pitch 상태
     */
    private val _pitch = MutableStateFlow(1f)

    val pitch: StateFlow<Float> = _pitch

    /**
     * 번역 로딩 상태
     */
    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating

    /**
     * 번역 전 텍스트
     */
    private val _originalText = MutableStateFlow("")
    val originalText: StateFlow<String> = _originalText


    /**
     * 번역 후 텍스트
     */
    private val _translatedText = MutableStateFlow("")
    val translatedText: StateFlow<String> = _translatedText


    /**
     * 텍스트 변경
     */
      fun updateScript(text: String) {
        //_script.value = text
        _originalText.value = text
    }

    /**
     * 속도 변경
     */
    fun setSpeechRate(rate: Float) {

        _speechRate.value = rate

    }



    /**
     * Pitch 변경
     */
    fun setPitch(value: Float) {

        _pitch.value = value

    }



    /**
     * TTS 실행
     */
    fun speak() {

        val textToSpeak = originalText.value

        if (textToSpeak.isBlank()) return

        tts.speak(
            text = textToSpeak,
            rate = speechRate.value,
            pitch = pitch.value
        )
    }


    /**
     * Room DB 저장
     */
    fun saveDB() {

        viewModelScope.launch {

            repository.insert(

                ScriptEntity(

                    text = script.value

                )

            )

        }

    }



    /**
     * 🔥 대용량 소설 자동 생성
     *
     * ✔ 한국 10개
     * ✔ 미국 10개
     * ✔ txt + pdf 생성
     * ✔ 약 50,000자 자동 확장
     *
     * ⚠ Settings 화면에서만 호출
     */
    fun createLargeSampleNovels() {

        viewModelScope.launch(Dispatchers.IO) {

            val koreanTitles = (1..10).map {
                "한국대작소설_$it"
            }

            val americanTitles = (1..10).map {
                "American_Epic_Novel_$it"
            }

            koreanTitles.forEach { title ->
                generateNovel(title, isKorean = true)
            }

            americanTitles.forEach { title ->
                generateNovel(title, isKorean = false)
            }
        }
    }

    /**
     * 소설 본문 자동 생성
     */
    private fun generateNovel(
        title: String,
        isKorean: Boolean
    ) {

        val content = buildLargeContent(isKorean)

        // txt 생성
        val txtFile =
            FileUtil.createTxtFile(application, title)

        TxtUtil.write(txtFile, content)

        // pdf 생성
        val pdfFile =
            FileUtil.createPdfFile(application, title)

        PdfUtil.write(pdfFile, content)
    }

    /**
     * 🔥 50,000자 자동 생성기
     */
    private fun buildLargeContent(isKorean: Boolean): String {

        val builder = StringBuilder()

        val paragraph = if (isKorean) {
            """
        서울의 밤은 깊어가고 있었다.
        바람은 차가웠고, 거리의 불빛은 흐릿했다.
        그는 오래된 기억을 떠올리며 천천히 걸었다.
        세상은 변했지만 그의 마음은 여전히 그 자리에 머물러 있었다.
        
        """.trimIndent()
        } else {
            """
        The night in New York was heavy and silent.
        The wind whispered through empty streets.
        He walked slowly, remembering a past that refused to fade.
        The world had changed, but his heart remained the same.
        
        """.trimIndent()
        }

        // 🔥 약 50,000자 되도록 반복 확장
        while (builder.length < 50000) {
            builder.append(paragraph)
        }

        return builder.toString()
    }

    /**
     * 파일 내용 열기
     *
     * txt / pdf 모두 지원
     *
     * ✔ Application Context 사용
     * ✔ PDFBox 기반 텍스트 추출
     */
    fun openFile(file: File) {

        viewModelScope.launch(Dispatchers.IO) {

            val content = when (file.extension.lowercase()) {

                "txt" -> {
                    // TXT 파일 읽기
                    TxtUtil.read(file)
                }

                "pdf" -> {
                    // 🔥 PDFBox 사용
                    PdfUtil.extractTextFromPdf(
                        application,   // ✔ Hilt로 주입된 Application 사용
                        application.contentResolver,
                        Uri.fromFile(file)
                    )
                }

                else -> ""
            }

            // 🔥 UI 상태는 Main에서 변경
            withContext(Dispatchers.Main) {
                _originalText.value = content
            }
        }
    }

    /**
     * 파일 목록 상태
     */
    private val _fileList =
        MutableStateFlow<List<File>>(emptyList())

    val fileList: StateFlow<List<File>> =
        _fileList



    /**
     * 파일 목록 로드
     */
    fun loadFiles() {

        viewModelScope.launch(Dispatchers.IO) {

            _fileList.value =
                FileUtil.getFileList(application)

        }

    }



    /**
     * 파일 삭제
     */
    fun deleteFile(file: File) {

        viewModelScope.launch(Dispatchers.IO) {

            FileUtil.delete(file)

            loadFiles()

        }

    }

    /**
     * SAF 기반 파일 열기 (완전 안전 버전)
     *
     * ✔ txt
     * ✔ pdf
     */
    fun openFileFromUri(
        resolver: ContentResolver,
        uri: Uri
    ) {

        viewModelScope.launch {

            val content = withContext(Dispatchers.IO) {

                when {

                    uri.toString().endsWith(".pdf", true) -> {

                        // 🔥 PDFBox 사용
                        PdfUtil.extractTextFromPdf(
                            application,
                            resolver,
                            uri
                        )
                    }

                    else -> {
                        // TXT 읽기
                        resolver.openInputStream(uri)
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            ?: ""
                    }
                }
            }

            _originalText.value = content
        }
    }

    /**
     * 한글 포함 여부 확인
     */
    private fun containsKorean_(text: String): Boolean {

        val regex = Regex("[ㄱ-ㅎㅏ-ㅣ가-힣]")

        return regex.containsMatchIn(text)

    }



    fun translate() {

        viewModelScope.launch(Dispatchers.IO) {

            _isTranslating.value = true

            val original = originalText.value

            val sourceLang =
                if (containsKorean(original))
                    TranslateLanguage.KOREAN
                else
                    TranslateLanguage.ENGLISH

            val targetLang =
                if (sourceLang == TranslateLanguage.KOREAN)
                    TranslateLanguage.ENGLISH
                else
                    TranslateLanguage.KOREAN

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()

            val translator = Translation.getClient(options)

            try {

                translator.downloadModelIfNeeded().await()

                val result =
                    translator.translate(original).await()

                withContext(Dispatchers.Main) {
                    _translatedText.value = result
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    _translatedText.value = "번역 실패: ${e.message}"
                }

            } finally {

                translator.close()

                withContext(Dispatchers.Main) {
                    _isTranslating.value = false
                }

            }

        }

    }

    /**
     * 한글 포함 여부 체크
     */
    private fun containsKorean(text: String): Boolean {

        val regex = Regex("[ㄱ-ㅎㅏ-ㅣ가-힣]")
        return regex.containsMatchIn(text)

    }

    fun preloadTranslationModel() {

        viewModelScope.launch(Dispatchers.IO) {

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.KOREAN)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()

            val translator = Translation.getClient(options)

            translator.downloadModelIfNeeded().await()

            translator.close()
        }

    }
}