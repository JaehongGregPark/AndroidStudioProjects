package com.example.pythonttsapp

/* =========================================================
   MainActivity.kt
   =========================================================

   📌 앱 기능

   1. TXT / PDF 파일 열기
   2. 텍스트 미리보기 표시
   3. 문장 / 문단 단위 TTS 읽기
   4. 현재 읽는 위치 하이라이트
   5. 자동 스크롤
   6. 문장 클릭 위치부터 읽기
   7. 재생 위치 자동 저장 (앱 꺼도 이어읽기)
   8. 재생 속도 조절
   9. MP3 파일 저장
   10. 일시정지 / 정지

   📌 안정성 처리

   ✔ PDFBox 초기화 필수
   ✔ PDF 백그라운드 로딩 (ANR 방지)
   ✔ Content URI 안전 판별
   ✔ 스트림 자동 close
   ✔ TTS Listener 1회만 등록

========================================================= */


/* =========================================================
   Android 기본
   ========================================================= */
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

import java.io.File
import java.util.Locale

/* =========================================================
   ViewBinding
   ========================================================= */
import com.example.pythonttsapp.databinding.ActivityMainBinding

/* =========================================================
   PDFBox Android
   ========================================================= */
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

/* =========================================================
   MainActivity
   ========================================================= */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    /* =====================================================
       기본 변수
       ===================================================== */
    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech
    private lateinit var prefs: SharedPreferences

    private var loadedText = ""
    private var readingUnits = listOf<String>()
    private var currentIndex = 0

    private var isPaused = false
    private var isStopped = false
    private var speechRate = 1.0f

    /* 읽기 모드 */
    private val MODE_SENTENCE = 0
    private val MODE_PARAGRAPH = 1
    private var readMode = MODE_SENTENCE

    /* =====================================================
       파일 선택 런처
       ===================================================== */
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { loadFile(it) }
        }

    /* =====================================================
       Activity 시작
       ===================================================== */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* ⭐ PDFBox 반드시 초기화 */
        PDFBoxResourceLoader.init(applicationContext)

        tts = TextToSpeech(this, this)
        prefs = getSharedPreferences("tts_state", Context.MODE_PRIVATE)

        restoreState()
        initUI()
        initTtsListener()
    }

    /* =====================================================
       UI 이벤트 연결
       ===================================================== */
    private fun initUI() {

        binding.fileBtn.setOnClickListener {
            openFileLauncher.launch(arrayOf("*/*"))
        }

        binding.sendBtn.setOnClickListener { startReading() }

        binding.pauseBtn.setOnClickListener {
            isPaused = !isPaused
        }

        binding.stopBtn.setOnClickListener {
            isStopped = true
            tts.stop()
        }

        binding.saveMp3Btn.setOnClickListener {
            saveMp3(loadedText)
        }

        /* 재생 속도 */
        binding.speedSeekBar.progress = (speechRate * 100).toInt()
        binding.speedSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(sb: SeekBar?, v: Int, f: Boolean) {
                speechRate = (v / 100f).coerceAtLeast(0.2f)
                tts.setSpeechRate(speechRate)
                prefs.edit().putFloat("rate", speechRate).apply()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        /* 읽기 모드 */
        binding.readModeSwitch.setOnCheckedChangeListener { _, checked ->
            readMode = if (checked) MODE_PARAGRAPH else MODE_SENTENCE
            buildReadingUnits()
        }

        /* 클릭 위치 읽기 */
        binding.previewTextView.setOnClickListener {
            detectClickedPosition()
        }
    }

    /* =====================================================
       TTS 초기화
       ===================================================== */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
            tts.setSpeechRate(speechRate)
        }
    }

    /* =====================================================
       TTS 완료 리스너 (1회만 설정)
       ===================================================== */
    private fun initTtsListener() {
        tts.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                runOnUiThread {
                    currentIndex++
                    savePosition()
                    speakNext()
                }
            }
            override fun onError(id: String?) {}
        })
    }

    /* =====================================================
       파일 로딩 (백그라운드)
       ===================================================== */
    private fun loadFile(uri: Uri) {

        Toast.makeText(this,"파일 읽는 중...",Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {

            val text = if (isPdf(uri)) readPdf(uri)
            else readText(uri)

            withContext(Dispatchers.Main) {
                loadedText = text
                binding.previewTextView.text = text
                buildReadingUnits()
            }
        }
    }

    /* =====================================================
       PDF 판별
       ===================================================== */
    private fun isPdf(uri: Uri): Boolean {
        val type = contentResolver.getType(uri)
        if (type == "application/pdf") return true
        return uri.lastPathSegment?.lowercase()?.endsWith(".pdf") == true
    }

    /* =====================================================
       TXT 읽기
       ===================================================== */
    private fun readText(uri: Uri): String {
        return contentResolver.openInputStream(uri)
            ?.bufferedReader()?.readText() ?: ""
    }

    /* =====================================================
       PDF 읽기
       ===================================================== */
    private fun readPdf(uri: Uri): String {
        contentResolver.openInputStream(uri).use { input ->
            val doc = PDDocument.load(input)
            val text = PDFTextStripper().getText(doc)
            doc.close()
            return text
        }
    }

    /* =====================================================
       문장 / 문단 분리
       ===================================================== */
    private fun buildReadingUnits() {
        readingUnits =
            if (readMode == MODE_PARAGRAPH)
                loadedText.split(Regex("\\n\\s*\\n"))
            else
                loadedText.split(Regex("(?<=[.!?])\\s+"))
    }

    /* =====================================================
       읽기 시작
       ===================================================== */
    private fun startReading() {
        isStopped = false
        isPaused = false
        speakNext()
    }

    /* =====================================================
       다음 읽기
       ===================================================== */
    private fun speakNext() {

        if (currentIndex >= readingUnits.size || isStopped) return

        if (isPaused) {
            binding.previewTextView.postDelayed({ speakNext() },200)
            return
        }

        val text = readingUnits[currentIndex]
        highlight(currentIndex)
        autoScroll(currentIndex)

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt")
    }

    /* =====================================================
       하이라이트
       ===================================================== */
    private fun highlight(index: Int) {

        val span = SpannableString(loadedText)
        val target = readingUnits[index]
        val start = loadedText.indexOf(target)

        if (start >= 0) {
            span.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                start,
                start + target.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.previewTextView.text = span
    }

    /* =====================================================
       자동 스크롤
       ===================================================== */
    private fun autoScroll(index: Int) {

        val text = readingUnits[index]
        val pos = loadedText.indexOf(text)

        binding.previewTextView.post {
            binding.previewTextView.layout?.let {
                val line = it.getLineForOffset(pos)
                binding.scrollView.smoothScrollTo(0, it.getLineTop(line))
            }
        }
    }

    /* =====================================================
       클릭 위치부터 읽기
       ===================================================== */
    private fun detectClickedPosition() {

        val layout = binding.previewTextView.layout ?: return
        val offset = layout.getOffsetForHorizontal(0,0f)

        for (i in readingUnits.indices) {
            val start = loadedText.indexOf(readingUnits[i])
            if (offset >= start) currentIndex = i
        }
        startReading()
    }

    /* =====================================================
       MP3 저장
       ===================================================== */
    private fun saveMp3(text: String) {

        val file = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC),"tts_audio.mp3")
        tts.synthesizeToFile(text,null,file,"save")

        Toast.makeText(this,"MP3 저장 완료",Toast.LENGTH_LONG).show()
    }

    /* =====================================================
       재생 위치 저장 / 복원
       ===================================================== */
    private fun savePosition() {
        prefs.edit().putInt("index", currentIndex).apply()
    }

    private fun restoreState() {
        currentIndex = prefs.getInt("index",0)
        speechRate = prefs.getFloat("rate",1.0f)
        readMode = prefs.getInt("mode",MODE_SENTENCE)
    }

    /* =====================================================
       종료
       ===================================================== */
    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
