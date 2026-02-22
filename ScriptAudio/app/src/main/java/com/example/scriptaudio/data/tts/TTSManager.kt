package com.example.scriptaudio.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale

@Singleton
class TTSManager @Inject constructor(

    @ApplicationContext private val context: Context

) {

    private var tts: TextToSpeech? = null

    init {

        tts = TextToSpeech(context) {

            if (it == TextToSpeech.SUCCESS) {

                tts?.language = Locale.KOREAN

            }

        }

    }

    fun speak(text: String, rate: Float, pitch: Float) {

        tts?.setSpeechRate(rate)

        tts?.setPitch(pitch)

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

    }

}