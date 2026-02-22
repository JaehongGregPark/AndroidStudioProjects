package com.example.scriptaudio.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scriptaudio.data.local.ScriptEntity
import com.example.scriptaudio.data.local.ScriptRepository
import com.example.scriptaudio.tts.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * ============================================
 * MainViewModel
 * ============================================
 *
 * 앱의 핵심 비즈니스 로직 담당
 *
 * 기능:
 *
 * ✔ TTS 읽기
 * ✔ TXT 불러오기
 * ✔ TXT 저장
 * ✔ PDF 저장
 * ✔ Room DB 저장
 * ✔ Speech Rate
 * ✔ Pitch
 * ✔ Script 상태관리
 *
 */
@HiltViewModel
class MainViewModel @Inject constructor(

    private val repository: ScriptRepository,
    private val ttsManager: TTSManager

) : ViewModel() {

    /**
     * =====================================
     * 현재 스크립트 상태
     * =====================================
     */
    private val _script = MutableStateFlow("")

    val script: StateFlow<String> = _script


    /**
     * =====================================
     * TTS Speed
     * =====================================
     */
    val speechRate = MutableStateFlow(1.0f)


    /**
     * =====================================
     * TTS Pitch
     * =====================================
     */
    val pitch = MutableStateFlow(1.0f)



    /**
     * =====================================
     * 텍스트 변경
     * =====================================
     *
     * MainScreen 입력시 호출됨
     */
    fun updateScript(text: String) {

        _script.value = text

    }



    /**
     * =====================================
     * TTS 읽기
     * =====================================
     */
    fun speak() {

        ttsManager.speak(

            text = script.value,
            rate = speechRate.value,
            pitch = pitch.value

        )

    }



    /**
     * =====================================
     * TXT 파일 불러오기
     * =====================================
     */
    fun loadTxt(

        resolver: ContentResolver,
        uri: Uri

    ) {

        viewModelScope.launch {

            val reader = BufferedReader(

                InputStreamReader(
                    resolver.openInputStream(uri)
                )

            )

            val text = reader.readText()

            reader.close()

            _script.value = text

        }

    }



    /**
     * =====================================
     * TXT 파일 저장
     * =====================================
     */
    fun saveTxt(context: Context) {

        viewModelScope.launch {

            val file = File(

                context.getExternalFilesDir(null),
                "script.txt"

            )

            val output = FileOutputStream(file)

            output.write(script.value.toByteArray())

            output.close()

        }

    }



    /**
     * =====================================
     * PDF 저장
     * =====================================
     */
    fun savePdf(context: Context) {

        viewModelScope.launch {

            val document = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(
                595,
                842,
                1
            ).create()

            val page = document.startPage(pageInfo)

            val canvas = page.canvas

            val paint = android.graphics.Paint()

            paint.textSize = 12f


            var y = 50

            script.value.lines().forEach {

                canvas.drawText(it, 50f, y.toFloat(), paint)

                y += 20

            }

            document.finishPage(page)


            val file = File(

                context.getExternalFilesDir(null),
                "script.pdf"

            )

            document.writeTo(FileOutputStream(file))

            document.close()

        }

    }



    /**
     * =====================================
     * Room DB 저장
     * =====================================
     */
    fun saveScriptToDB() {

        viewModelScope.launch {

            repository.insert(

                ScriptEntity(

                    text = script.value

                )

            )

        }

    }


}