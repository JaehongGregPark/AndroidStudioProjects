/* =========================================================
   Android 기본 라이브러리
   ========================================================= */
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.graphics.Color
import java.io.File
import java.util.Locale

/* =========================================================
   AndroidX
   ========================================================= */
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/* =========================================================
   Chaquopy Python
   ========================================================= */
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

/* =========================================================
   ViewBinding
   ========================================================= */
import com.example.pythonttsapp.databinding.ActivityMainBinding


/**
 * =========================================================
 * MainActivity
 * =========================================================
 *
 * 기능
 * 1. txt 파일 선택 후 미리보기
 * 2. 한국어 / 영어 혼합 TTS
 * 3. 현재 읽는 문장 하이라이트 표시
 * 4. 일시정지 / 정지
 * 5. MP3 파일 저장
 *
 */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    /* =========================================================
       ViewBinding & 상태 변수
       ========================================================= */

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech

    private var isPaused = false
    private var isStopped = false

    // 문장 리스트 (하이라이트용)
    private var sentenceList = listOf<String>()
    private var currentSentenceIndex = 0


    /* =========================================================
       TXT 파일 선택 런처
       ========================================================= */
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                val text = readTextFromUri(it)
                binding.previewTextView.text = text
            }
        }


    /* =========================================================
       Lifecycle
       ========================================================= */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TTS 초기화
        tts = TextToSpeech(this, this)

        // Python 초기화
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        /* ===========================
           버튼 이벤트
           =========================== */

        // 파일 선택
        binding.fileBtn.setOnClickListener {
            openFileLauncher.launch(arrayOf("text/plain"))
        }

        // TTS 재생
        binding.sendBtn.setOnClickListener {
            val text = binding.previewTextView.text.toString()
            if (text.isNotBlank()) speakMixedText(text)
        }

        // 일시정지
        binding.pauseBtn.setOnClickListener {
            isPaused = !isPaused
        }

        // 정지
        binding.stopBtn.setOnClickListener {
            isStopped = true
            isPaused = false
            tts.stop()
        }

        // MP3 저장
        binding.saveMp3Btn.setOnClickListener {
            val text = binding.previewTextView.text.toString()
            if (text.isNotBlank()) saveTtsAsMp3(text)
        }
    }


    /* =========================================================
       TTS 초기화 완료
       ========================================================= */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setSpeechRate(0.85f)
        }
    }


    /* =========================================================
       TTS 재생 (하이라이트 포함)
       ========================================================= */
    private fun speakMixedText(inputText: String) {

        isStopped = false
        isPaused = false

        // 문장 분리 (간단 분리)
        sentenceList = inputText.split(Regex("(?<=[.!?])\\s+"))
        currentSentenceIndex = 0

        Thread {

            val py = Python.getInstance()
            val module = py.getModule("tts_utils")

            val result = module.callAttr("split_korean_english", inputText)

            var utteranceIndex = 0

            for (item in result.asList()) {

                if (isStopped) break
                while (isPaused) Thread.sleep(100)

                val lang = item.asList()[0].toString()
                val text = item.asList()[1].toString()

                val utteranceId = "utt_$utteranceIndex"

                runOnUiThread {

                    // 언어 설정
                    if (lang == "ko") {
                        tts.language = Locale.KOREAN
                        tts.setSpeechRate(0.9f)
                    } else {
                        tts.language = Locale.US
                        tts.setSpeechRate(0.8f)
                    }

                    // 진행 리스너 (하이라이트)
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

                        override fun onStart(id: String?) {
                            runOnUiThread {
                                highlightSentence(currentSentenceIndex)
                                currentSentenceIndex++
                            }
                        }

                        override fun onDone(id: String?) {}
                        override fun onError(id: String?) {}
                    })

                    tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
                }

                utteranceIndex++
                Thread.sleep(200)
            }

        }.start()
    }


    /* =========================================================
       문장 하이라이트 UI
       ========================================================= */
    private fun highlightSentence(index: Int) {

        val fullText = binding.previewTextView.text.toString()
        val spannable = SpannableString(fullText)

        if (index >= sentenceList.size) return

        val target = sentenceList[index]
        val start = fullText.indexOf(target)

        if (start >= 0) {
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                start,
                start + target.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.previewTextView.text = spannable
    }


    /* =========================================================
       MP3 파일 저장
       ========================================================= */
    private fun saveTtsAsMp3(text: String) {

        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            "tts_output.mp3"
        )

        tts.synthesizeToFile(
            text,
            null,
            file,
            "save_mp3"
        )

        binding.previewTextView.text =
            "MP3 저장 완료:\n${file.absolutePath}"
    }


    /* =========================================================
       TXT 파일 읽기
       ========================================================= */
    private fun readTextFromUri(uri: Uri): String {
        return try {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                it.readText()
            } ?: "파일을 읽을 수 없습니다"
        } catch (e: Exception) {
            "파일 읽기 오류: ${e.message}"
        }
    }


    /* =========================================================
       종료 처리
       ========================================================= */
    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}
