package com.example.pythonttsapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.pythonttsapp.databinding.ActivityMainBinding
import android.speech.tts.TextToSpeech
import java.util.Locale

class MainActivity : AppCompatActivity() , TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        binding.sendBtn.setOnClickListener {
            val inputText = binding.inputEditText.text.toString()

            if (inputText.isBlank()) return@setOnClickListener

            speakMixedText(inputText)
        }
            //tts.speak(
            //    input,
            //    TextToSpeech.QUEUE_FLUSH,
            //    null,
            //    "TTS_ID"
            //)



    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            //tts.language = Locale.KOREAN
            tts.language = Locale.US   // 영어 기준
            tts.setSpeechRate(0.8f)    // 기본 1.0 → 느리게
            tts.setPitch(1.0f)         // 음높이 (0.5 ~ 2.0)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }

    private fun speakMixedText(inputText: String) {
        val py = Python.getInstance()
        val module = py.getModule("tts_utils")

        val result = module.callAttr("split_korean_english", inputText)

        for (item in result.asList()) {
            val lang = item.asList()[0].toString()
            val text = item.asList()[1].toString()

            if (lang == "ko") {
                tts.language = Locale.KOREAN
                tts.setSpeechRate(0.85f)
            } else {
                tts.language = Locale.US
                tts.setSpeechRate(0.75f)
            }

            tts.speak(text, TextToSpeech.QUEUE_ADD, null, null)
        }
    }
}