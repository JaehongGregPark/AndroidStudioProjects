package com.example.pythonttsapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.pythonttsapp.databinding.ActivityMainBinding
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.launch
import java.util.Locale

/* =========================================================
   MainActivity.kt
   =========================================================

   📌 UI 계층

   역할:
   - 사용자 입력 처리
   - 화면 표시
   - TTS 실행

   ViewModel 상태를 관찰만 함

========================================================= */

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech

    /* ViewModel 연결 */
    private val viewModel: MainViewModel by viewModels()

    /* 파일 선택 런처 */
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.loadFile(contentResolver, it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* PDFBox 초기화 */
        PDFBoxResourceLoader.init(applicationContext)

        /* TTS 초기화 */
        tts = TextToSpeech(this, this)

        observeState()
        initUI()
    }

    /* =====================================================
       ViewModel 상태 관찰
       ===================================================== */
    private fun observeState() {

        lifecycleScope.launch {

            viewModel.uiState.collect { state ->

                // 텍스트 표시
                binding.previewTextView.text = state.text
            }
        }
    }

    /* =====================================================
       UI 이벤트 연결
       ===================================================== */
    private fun initUI() {

        binding.fileBtn.setOnClickListener {
            openFileLauncher.launch(arrayOf("*/*"))
        }

        binding.sendBtn.setOnClickListener {
            speakNext()
        }
    }

    /* =====================================================
       다음 문장 읽기
       ===================================================== */
    private fun speakNext() {

        val text = viewModel.getCurrentUnit() ?: return

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt")

        viewModel.next()
    }

    /* =====================================================
       TTS 초기화 완료
       ===================================================== */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
