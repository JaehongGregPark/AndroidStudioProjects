/* =========================
   Android 기본 라이브러리
   ========================= */
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech

/* =========================
   AndroidX 라이브러리
   ========================= */
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/* =========================
   서드파티 라이브러리
   ========================= */
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

/* =========================
   프로젝트 내부
   ========================= */
import com.example.pythonttsapp.databinding.ActivityMainBinding

/* =========================
   Java 표준
   ========================= */
import java.util.Locale


/**
 * 메인 액티비티
 * - 한국어/영어 혼합 텍스트 TTS
 * - txt 파일 선택 후 미리보기 및 음성 출력
 * - 일시정지 / 정지 지원
 */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    /* =========================
       View & 상태 변수
       ========================= */

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech

    // TTS 제어용 상태 플래그
    private var isPaused = false
    private var isStopped = false


    /* =========================
       파일 선택 런처 (SAF)
       ========================= */

    // txt 파일 선택 → 미리보기 영역에 내용 표시
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                val text = readTextFromUri(it)
                binding.previewTextView.text = text
            }
        }


    /* =========================
       Lifecycle
       ========================= */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TTS 초기화
        tts = TextToSpeech(this, this)

        // Chaquopy(Python) 초기화 (앱 실행 시 1회)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        /* =========================
           버튼 이벤트 처리
           ========================= */

        // 직접 입력한 텍스트 TTS 실행
        binding.sendBtn.setOnClickListener {
            val text = binding.previewTextView.text.toString()
            if (text.isNotBlank()) {
                speakMixedText(text)
            }
        }

        // 일시정지 토글
        binding.pauseBtn.setOnClickListener {
            isPaused = !isPaused
        }

        // 완전 정지
        binding.stopBtn.setOnClickListener {
            isStopped = true
            isPaused = false
            tts.stop()
        }

        // txt 파일 선택
        binding.fileBtn.setOnClickListener {
            openFileLauncher.launch(arrayOf("text/plain"))
        }
    }


    /**
     * TTS 엔진 초기화 완료 콜백
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setSpeechRate(0.8f)
        }
    }


    /* =========================
       TTS 핵심 로직
       ========================= */

    /**
     * Python을 이용해 한국어/영어를 분리한 뒤
     * 언어별로 TTS 출력
     */
    private fun speakMixedText(inputText: String) {
        isStopped = false
        isPaused = false

        Thread {
            val py = Python.getInstance()
            val module = py.getModule("tts_utils")

            // Python 함수 호출 → [(lang, text), ...]
            val result = module.callAttr("split_korean_english", inputText)

            for (item in result.asList()) {

                // 정지 상태면 즉시 종료
                if (isStopped) break

                // 일시정지 상태면 대기
                while (isPaused) {
                    Thread.sleep(100)
                }

                val lang = item.asList()[0].toString()
                val text = item.asList()[1].toString()

                runOnUiThread {
                    if (lang == "ko") {
                        tts.language = Locale.KOREAN
                        tts.setSpeechRate(0.85f)
                    } else {
                        tts.language = Locale.US
                        tts.setSpeechRate(0.75f)
                    }

                    tts.speak(text, TextToSpeech.QUEUE_ADD, null, null)
                }

                // 문장 간 간격
                Thread.sleep(300)
            }
        }.start()
    }


    /* =========================
       파일 처리 유틸
       ========================= */

    /**
     * SAF(Uri) 기반 텍스트 파일 읽기
     */
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