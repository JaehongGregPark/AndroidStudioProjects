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

        tts.speak(

            text = script.value,

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
     *
     * 신규 소설 샘플 생성 함수
     *
     * 한국소설 3개
     * 미국소설 2개
     *
     * txt + pdf 생성
     *
     */
    fun createSampleNovels() {



        viewModelScope.launch(Dispatchers.IO) {



            val novelList = listOf(

                Pair(
                    "한국소설_1_구름위의약속",
                    "그녀는 구름 위에 앉아 있었다.\n서울의 밤은 조용했고, 그녀의 마음은 더 조용했다."
                ),

                Pair(
                    "한국소설_2_시간의끝",
                    "시간은 끝나지 않는다.\n우리가 끝날 뿐이다."
                ),

                Pair(
                    "한국소설_3_달빛거리",
                    "달빛이 거리를 비췄다.\n그의 그림자는 길게 늘어졌다."
                ),

                Pair(
                    "미국소설_1_The_Last_Promise",
                    "He stood alone in New York.\nThe city never cared."
                ),

                Pair(
                    "미국소설_2_Silent_Road",
                    "The road was silent.\nBut his mind was loud."
                )

            )

            novelList.forEach {

                val fileName = it.first
                val content = it.second

                /**
                 * txt 생성
                 */
                val txtFile =
                    FileUtil.createTxtFile(application, fileName)

                TxtUtil.write(
                    txtFile,
                    content
                )
              /**
                 * pdf 생성
                 */
                val pdfFile =
                    FileUtil.createPdfFile(application, fileName)

                PdfUtil.write(
                    pdfFile,
                    content
                )
         }
        }
    }
    /**
     * 파일 내용 열기
     *
     * txt / pdf 모두 지원
     */
    fun openFile(file: File) {

        viewModelScope.launch(Dispatchers.IO) {

            val content = when {

                file.extension.lowercase() == "txt" -> {

                    TxtUtil.read(file)

                }

                file.extension.lowercase() == "pdf" -> {

                    PdfUtil.read(file)

                }

                else -> ""

            }


            _script.value = content

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
     */
    fun openFileFromUri(
        resolver: ContentResolver,
        uri: Uri
    ) {

        viewModelScope.launch {

            val content = withContext(Dispatchers.IO) {

                resolver.openInputStream(uri)?.bufferedReader()?.use {

                    it.readText()

                } ?: ""

            }

            _script.value = content

        }

    }
    /**
     * 번역 기능
     *
     * 한글 포함 → 영어
     * 영어만 → 한글
     */
    fun translate_() {

        viewModelScope.launch {

            val originalText = script.value

            val translated = withContext(Dispatchers.Default) {

                if (containsKorean_(originalText)) {

                    translateToEnglish_(originalText)

                } else {

                    translateToKorean_(originalText)

                }

            }

            _script.value = translated

        }

    }



    /**
     * 한글 포함 여부 확인
     */
    private fun containsKorean_(text: String): Boolean {

        val regex = Regex("[ㄱ-ㅎㅏ-ㅣ가-힣]")

        return regex.containsMatchIn(text)

    }



    /**
     * 한글 → 영어 (데모 번역)
     */
    private fun translateToEnglish_(text: String): String {

        return text
            .replace("안녕하세요", "Hello")
            .replace("서울", "Seoul")
            .replace("사랑", "Love")
            .replace("시간", "Time")
            .replace("달빛", "Moonlight")
    }



    /**
     * 영어 → 한글 (데모 번역)
     */
    private fun translateToKorean_(text: String): String {

        return text
            .replace("Hello", "안녕하세요")
            .replace("Seoul", "서울")
            .replace("Love", "사랑")
            .replace("Time", "시간")
            .replace("Moonlight", "달빛")
    }

    /**
     * 실제 ML Kit 번역
     *
     * 한글 ↔ 영어 자동 감지
     */
    fun translate__() {

        viewModelScope.launch(Dispatchers.IO) {   // 🔥 IO로 변경

            val originalText = script.value

            val sourceLang =
                if (containsKorean(originalText))
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
                    translator.translate(originalText).await()

                withContext(Dispatchers.Main) {
                    _script.value = result   // 🔥 UI는 Main에서만
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    _script.value = "번역 실패: ${e.message}"
                }

            } finally {

                translator.close()

            }

        }

    }

    fun translate___() {

        viewModelScope.launch(Dispatchers.IO) {

            _isTranslating.value = true   // 🔥 로딩 시작

            val originalText = script.value

            val sourceLang =
                if (containsKorean(originalText))
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
                    translator.translate(originalText).await()

                withContext(Dispatchers.Main) {
                    _script.value = result
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    _script.value = "번역 실패: ${e.message}"
                }

            } finally {

                translator.close()

                withContext(Dispatchers.Main) {
                    _isTranslating.value = false   // 🔥 로딩 종료
                }

            }

        }

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