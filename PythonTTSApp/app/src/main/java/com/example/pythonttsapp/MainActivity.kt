package com.example.pythonttsapp

/* =========================================================
   MainActivity.kt
   =========================================================

   📌 기능 요약

   1. TXT / PDF 파일 열기
   2. 텍스트 미리보기 표시
   3. 한국어 영어 혼합 TTS 재생
   4. 문장 / 문단 읽기 모드
   5. 현재 읽는 위치 하이라이트
   6. 자동 스크롤
   7. 문장 클릭하면 해당 위치부터 읽기
   8. 재생 위치 자동 저장 (앱 꺼도 이어읽기)
   9. 재생 속도 슬라이더
   10. MP3 파일 저장 (PDF 포함)
   11. 일시정지 / 정지

========================================================= */


/* =========================================================
   Android 기본 라이브러리
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
import java.io.File
import java.util.Locale


/* =========================================================
   ViewBinding
   ========================================================= */
import com.example.pythonttsapp.databinding.ActivityMainBinding


/* =========================================================
   PDF 읽기 라이브러리
   (build.gradle에 pdfbox-android 필요)
   ========================================================= */
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper


/* =========================================================
   Chaquopy Python (언어 분리용)
   ========================================================= */
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform


/* =========================================================
   MainActivity
   ========================================================= */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    /* =====================================================
       ViewBinding / TTS / 상태 변수
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


    /* =====================================================
       읽기 모드
       ===================================================== */
    private val MODE_SENTENCE = 0
    private val MODE_PARAGRAPH = 1
    private var readMode = MODE_SENTENCE


    /* =====================================================
       파일 선택 런처
       ===================================================== */
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { loadFile(it) }
        }


    /* =====================================================
       Activity 시작
       ===================================================== */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)

        prefs = getSharedPreferences("tts_state", Context.MODE_PRIVATE)

        restoreState()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        initUI()
    }


    /* =====================================================
       UI 이벤트 설정
       ===================================================== */
    private fun initUI() {

        // 파일 열기
        binding.fileBtn.setOnClickListener {
            openFileLauncher.launch(arrayOf("*/*"))
        }

        // 재생
        binding.sendBtn.setOnClickListener {
            startReading()
        }

        // 일시정지
        binding.pauseBtn.setOnClickListener {
            isPaused = !isPaused
        }

        // 정지
        binding.stopBtn.setOnClickListener {
            isStopped = true
            tts.stop()
        }

        // MP3 저장
        binding.saveMp3Btn.setOnClickListener {
            saveMp3(loadedText)
        }

        // 재생 속도
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

        // 읽기 모드
        binding.readModeSwitch.setOnCheckedChangeListener { _, checked ->
            readMode = if (checked) MODE_PARAGRAPH else MODE_SENTENCE
            prefs.edit().putInt("mode", readMode).apply()
        }

        // 문장 클릭 재생
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
       파일 로드 (TXT / PDF)
       ===================================================== */
    private fun loadFile(uri: Uri) {

        loadedText = if (uri.toString().endsWith(".pdf"))
            readPdf(uri)
        else
            readText(uri)

        binding.previewTextView.text = loadedText
        buildReadingUnits()
    }


    /* =====================================================
       TXT 읽기
       ===================================================== */
    private fun readText(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
    }


    /* =====================================================
       PDF 읽기
       ===================================================== */
    private fun readPdf(uri: Uri): String {

        val input = contentResolver.openInputStream(uri)
        val doc = PDDocument.load(input)
        val text = PDFTextStripper().getText(doc)
        doc.close()
        return text
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
       다음 단위 읽기
       ===================================================== */
    private fun speakNext() {

        if (currentIndex >= readingUnits.size || isStopped) return

        if (isPaused) {
            binding.previewTextView.postDelayed({ speakNext() }, 200)
            return
        }

        val text = readingUnits[currentIndex]

        highlight(currentIndex)
        autoScroll(currentIndex)

        tts.setOnUtteranceProgressListener(object: UtteranceProgressListener(){

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

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt")
    }


    /* =====================================================
       하이라이트 표시
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
       클릭 위치 찾기
       ===================================================== */
    private fun detectClickedPosition() {

        val layout = binding.previewTextView.layout ?: return
        val line = layout.getLineForVertical(binding.previewTextView.scrollY)
        val offset = layout.getOffsetForHorizontal(line, 0f)

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

        val file = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "tts_audio.mp3")

        tts.synthesizeToFile(text, null, file, "save")

        Toast.makeText(this,"MP3 저장 완료",Toast.LENGTH_LONG).show()
    }


    /* =====================================================
       재생 위치 저장 / 복원
       ===================================================== */
    private fun savePosition() {
        prefs.edit().putInt("index", currentIndex).apply()
    }

    private fun restoreState() {
        currentIndex = prefs.getInt("index", 0)
        speechRate = prefs.getFloat("rate", 1.0f)
        readMode = prefs.getInt("mode", MODE_SENTENCE)
    }


    /* =====================================================
       종료 처리
       ===================================================== */
    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
