package com.example.pythonttsmvvmapp.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pythonttsmvvmapp.data.parser.PdfParser
import com.example.pythonttsmvvmapp.data.parser.TxtParser
import com.example.pythonttsmvvmapp.tts.TtsManager
import com.example.pythonttsmvvmapp.tts.TtsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI ↔ 파일 ↔ TTS 연결 담당
 *
 * 이제부터:
 * ✔ 최근 파일
 * ✔ 이어읽기
 * ✔ 마지막 위치 저장
 * 까지 관리한다.
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    private val txtParser: TxtParser,
    private val pdfParser: PdfParser
) : ViewModel() {

    // --------------------------------------------------
    // UI 상태
    // --------------------------------------------------

    val text = mutableStateOf("")
    val state = mutableStateOf<TtsState>(TtsState.Idle)

    val highlightStart = mutableStateOf(-1)
    val highlightEnd = mutableStateOf(-1)

    val fileName = mutableStateOf("")
    val currentUri = mutableStateOf<String?>(null)

    /** 최근 파일 목록 */
    val recentFiles = mutableStateOf<List<Pair<String, String>>>(emptyList())

    // --------------------------------------------------
    // 초기화
    // --------------------------------------------------

    init {
        /**
         * 읽는 위치 변경 → 하이라이트 반영
         */
        ttsManager.setOnRangeChanged { start, end ->
            highlightStart.value = start
            highlightEnd.value = end
        }
    }

    // --------------------------------------------------
    // 파일 이름 얻기
    // --------------------------------------------------

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) {
                name = it.getString(index)
            }
        }
        return name
    }

    // --------------------------------------------------
    // 파일 열기
    // --------------------------------------------------

    fun openFile(context: Context, uri: Uri) {
        viewModelScope.launch {

            val name = getFileName(context, uri)
            fileName.value = name
            currentUri.value = uri.toString()

            val result = when {
                name.endsWith(".txt", true) -> txtParser.parse(uri)
                name.endsWith(".pdf", true) -> pdfParser.parse(uri)
                else -> "지원하지 않는 파일 형식입니다."
            }

            text.value = result

            saveRecent(context, name, uri.toString())
            loadRecent(context)

            restoreLastPosition(context)
        }
    }

    // --------------------------------------------------
    // 최근 파일 관리
    // --------------------------------------------------

    private fun saveRecent(context: Context, name: String, uri: String) {
        val pref = context.getSharedPreferences("recent", Context.MODE_PRIVATE)
        val list = pref.getStringSet("list", mutableSetOf())!!.toMutableSet()

        // 중복 제거
        list.removeAll { it.endsWith(uri) }

        // 최신 파일 추가
        list.add("$name|$uri")

        // ⭐ Set → List 변환 후 마지막 10개 유지
        val trimmed = list.toList().takeLast(10).toSet()

        pref.edit().putStringSet("list", trimmed).apply()
    }
    fun loadRecent(context: Context) {
        val pref = context.getSharedPreferences("recent", Context.MODE_PRIVATE)
        val set = pref.getStringSet("list", emptySet()) ?: emptySet()

        recentFiles.value = set.mapNotNull {
            val sp = it.split("|")
            if (sp.size == 2) sp[0] to sp[1] else null
        }
    }

    // --------------------------------------------------
    // 이어읽기
    // --------------------------------------------------

    /**
     * 현재 위치 저장
     */
    private fun saveLastPosition(context: Context) {
        val uri = currentUri.value ?: return
        val pref = context.getSharedPreferences("last_read", Context.MODE_PRIVATE)

        pref.edit()
            .putInt("${uri}_start", highlightStart.value)
            .putInt("${uri}_end", highlightEnd.value)
            .apply()
    }

    /**
     * 마지막 위치 복원
     */
    private fun restoreLastPosition(context: Context) {
        val uri = currentUri.value ?: return
        val pref = context.getSharedPreferences("last_read", Context.MODE_PRIVATE)

        highlightStart.value = pref.getInt("${uri}_start", -1)
        highlightEnd.value = pref.getInt("${uri}_end", -1)
    }

    // --------------------------------------------------
    // TTS
    // --------------------------------------------------

    fun speak(context: Context) {
        if (text.value.isBlank()) return

        ttsManager.speak(text.value)
        state.value = TtsState.Speaking(IntRange(0, 0))
    }

    fun pause() {
        ttsManager.pause()
        state.value = TtsState.Paused
    }

    fun stop(context: Context) {
        ttsManager.stop()
        state.value = TtsState.Idle

        saveLastPosition(context)

        highlightStart.value = -1
        highlightEnd.value = -1
    }
}
