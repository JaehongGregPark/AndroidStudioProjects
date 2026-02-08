package com.example.pythonttsapp

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.pythonttsapp.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech
    private var isPaused = false
    private var isStopped = false

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

        binding.pauseBtn.setOnClickListener {
            isPaused = !isPaused
        }

        binding.stopBtn.setOnClickListener {
            isStopped = true
            isPaused = false
            tts.stop()
        }

        binding.loadFileBtn.setOnClickListener {
            val fileText = readTextFromAssets("sample.txt")
            if (fileText.isNotBlank()) {
                speakMixedText(fileText)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setSpeechRate(0.8f)
        }
    }

    private fun speakMixedText(inputText: String) {
        isStopped = false
        isPaused = false

        Thread {
            val py = Python.getInstance()
            val module = py.getModule("tts_utils")
            val result = module.callAttr("split_korean_english", inputText)

            for (item in result.asList()) {
                if (isStopped) break
                while (isPaused) Thread.sleep(100)

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

                Thread.sleep(300)
            }
        }.start()
    }

    private fun readTextFromAssets(fileName: String): String {
        return try {
            assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}