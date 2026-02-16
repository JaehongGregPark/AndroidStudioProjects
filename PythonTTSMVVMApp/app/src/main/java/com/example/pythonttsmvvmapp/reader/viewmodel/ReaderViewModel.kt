package com.example.pythonttsmvvmapp.reader.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pythonttsmvvmapp.reader.usecase.OpenFileUseCase
import com.example.pythonttsmvvmapp.reader.usecase.ReadingPositionUseCase
import com.example.pythonttsmvvmapp.reader.usecase.SpeakUseCase
import com.example.pythonttsmvvmapp.tts.TtsManager
import com.example.pythonttsmvvmapp.tts.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 이제 진짜 UI 전용 ViewModel
 * 👉 계산 / 저장 / 파싱 없음
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val openFileUseCase: OpenFileUseCase,
    private val speakUseCase: SpeakUseCase,
    private val readingPositionUseCase: ReadingPositionUseCase,
    private val ttsManager: TtsManager
) : ViewModel() {

    val text = mutableStateOf("")
    val state = mutableStateOf<TtsState>(TtsState.Idle)

    val highlightStart = mutableStateOf(-1)
    val highlightEnd = mutableStateOf(-1)

    val fileName = mutableStateOf("")
    val currentUri = mutableStateOf<String?>(null)

    init {
        ttsManager.setOnRangeChanged { s, e ->
            highlightStart.value = s
            highlightEnd.value = e
        }
    }

    fun openFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            val (name, parsedText) = openFileUseCase(context, uri)

            fileName.value = name
            text.value = parsedText
            currentUri.value = uri.toString()

            currentUri.value?.let {
                val (s, e) = readingPositionUseCase.restore(context, it)
                highlightStart.value = s
                highlightEnd.value = e
            }
        }
    }

    fun speak() {
        if (text.value.isBlank()) return

        speakUseCase(text.value)
        state.value = TtsState.Speaking(IntRange(0, 0))
    }

    fun pause() {
        ttsManager.pause()
        state.value = TtsState.Paused
    }

    fun stop(context: Context) {
        ttsManager.stop()
        state.value = TtsState.Idle

        currentUri.value?.let {
            readingPositionUseCase.save(
                context,
                it,
                highlightStart.value,
                highlightEnd.value
            )
        }

        highlightStart.value = -1
        highlightEnd.value = -1
    }
}
