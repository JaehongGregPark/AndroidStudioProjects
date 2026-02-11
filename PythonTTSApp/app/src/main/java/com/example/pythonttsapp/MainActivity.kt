package com.example.pythonttsapp

/* =========================
   Android 기본 라이브러리
   ========================= */
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan

/* =========================
   AndroidX
   ========================= */
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/* =========================
   Chaquopy (Python)
   ========================= */
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

/* =========================
   ViewBinding
   ========================= */
import com.example.pythonttsapp.databinding.ActivityMainBinding

import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    /* =========================
       View & 상태 변수
       ========================= */

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech

    private var isPaused = false
    private var isStopped = false

    private var currentFullText: String = ""

    // 문장 리스트 (lang, text)
    private var sentenceList: List<Pair<String, String>> = emptyList()

    private var currentIndex = 0

    // 하이라이트 최적화용
    private var spannable: SpannableString? = null
    private var currentSpan: BackgroundColorSpan? = null

    /* =========================
       파일 선택
       ========================= */

    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                val text = readTextFromUri(it)
                currentFullText = text
                binding.previewTextView.text = text
            }
        }

    /* =========================
       Lifecycle
       ========================= */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)

        // Python 초기화
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        /* =========================
           TTS 완료 콜백 기반 처리
           ========================= */

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    speakNext()
                }
            }

            override fun onError(utteranceId: String?) {}
        })

        /* =========================
           버튼 이벤트
           ========================= */

        // 재생
        binding.sendBtn.setOnClickListener {
            val text = binding.previewTextView.text.toString()
            if (text.isNotBlank()) {
                currentFullText = text
                speakMixedText(text)
            }
        }

        // 일시정지 / 재개
        binding.pauseBtn.setOnClickListener {

            if (!isPaused) {
                isPaused = true
                tts.stop()
            } else {
                isPaused = false
                speakNext()
            }
        }

        // 정지
        binding.stopBtn.setOnClickListener {
            stopAll()
        }

        // 파일 선택
        binding.fileBtn.setOnClickListener {
            openFileLauncher.launch(arrayOf("text/plain"))
        }
    }

    /* =========================
       TTS 초기화 완료
       ========================= */

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setSpeechRate(0.8f)
        }
    }

    /* =========================
       핵심 TTS 로직
       ========================= */

    /**
     * Python에서 문장 분리 후
     * 첫 문장 재생 시작
     */
    private fun speakMixedText(inputText: String) {

        isStopped = false
        isPaused = false
        currentIndex = 0

        Thread {

            val py = Python.getInstance()
            val module = py.getModule("tts_utils")

            val result = module.callAttr("split_korean_english", inputText)

            sentenceList = result.asList().map {
                val lang = it.asList()[0].toString()
                val text = it.asList()[1].toString()
                Pair(lang, text)
            }

            runOnUiThread {
                spannable = SpannableString(currentFullText)
                binding.previewTextView.text = spannable
                speakNext()
            }

        }.start()
    }

    /**
     * 다음 문장 재생 (콜백 기반)
     */
    private fun speakNext() {

        if (isStopped) return
        if (isPaused) return
        if (currentIndex >= sentenceList.size) return

        val (lang, text) = sentenceList[currentIndex]

        // 언어 설정
        if (lang == "ko") {
            tts.language = Locale.KOREAN
            tts.setSpeechRate(0.85f)
        } else {
            tts.language = Locale.US
            tts.setSpeechRate(0.75f)
        }

        highlightSentence(text)

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "utterance_$currentIndex"
        )

        currentIndex++
    }

    /* =========================
       하이라이트 최적화
       ========================= */

    private fun highlightSentence(sentence: String) {

        val fullText = currentFullText
        val start = fullText.indexOf(sentence)
        if (start < 0) return

        val end = start + sentence.length

        currentSpan?.let {
            spannable?.removeSpan(it)
        }

        val newSpan = BackgroundColorSpan(Color.YELLOW)

        spannable?.setSpan(
            newSpan,
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        currentSpan = newSpan

        binding.previewTextView.text = spannable
    }

    /* =========================
       정지 처리
       ========================= */

    private fun stopAll() {
        isStopped = true
        isPaused = false
        currentIndex = 0
        tts.stop()

        binding.previewTextView.text = currentFullText
    }

    /* =========================
       파일 읽기
       ========================= */

    private fun readTextFromUri(uri: Uri): String {
        return try {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                it.readText()
            } ?: "파일을 읽을 수 없습니다."
        } catch (e: Exception) {
            "파일 읽기 오류: ${e.message}"
        }
    }

    /* =========================
       종료 처리
       ========================= */

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}
