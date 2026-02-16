package com.example.pythonttsmvvmapp.reader.usecase

import com.example.pythonttsmvvmapp.tts.TtsManager
import javax.inject.Inject

/**
 * TTS 재생 담당
 */
class SpeakUseCase @Inject constructor(
    private val ttsManager: TtsManager
) {
    operator fun invoke(text: String) {
        ttsManager.speak(text)
    }
}
