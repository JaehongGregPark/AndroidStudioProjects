package com.example.scriptaudio.viewmodel

import android.app.Application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.scriptaudio.data.local.ScriptEntity
import com.example.scriptaudio.data.local.ScriptRepository
import com.example.scriptaudio.tts.TTSManager

import com.example.scriptaudio.util.FileUtil
import com.example.scriptaudio.util.TxtUtil
import com.example.scriptaudio.util.PdfUtil

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


/**
 *
 * MainViewModel
 *
 * HiltViewModel 에서는
 * getApplication() 사용하지 않고
 *
 * Application 을 직접 주입 받아야 함
 *
 */
@HiltViewModel
class MainViewModel @Inject constructor(

    /**
     * Application Context
     *
     * ✔ getApplication 대신 사용
     */
    private val application: Application,


    /**
     * Room Repository
     */
    private val repository: ScriptRepository,


    /**
     * TTS Manager
     */
    private val tts: TTSManager

) : ViewModel() {



    /**
     * 현재 스크립트 텍스트
     */
    private val _script = MutableStateFlow("")

    val script: StateFlow<String> = _script



    /**
     * TTS 속도 상태
     */
    private val _speechRate = MutableStateFlow(1f)

    val speechRate: StateFlow<Float> = _speechRate



    /**
     * TTS Pitch 상태
     */
    private val _pitch = MutableStateFlow(1f)

    val pitch: StateFlow<Float> = _pitch



    /**
     * 텍스트 변경
     */
    fun updateScript(text: String) {

        _script.value = text

    }



    /**
     * 속도 변경
     */
    fun setSpeechRate(rate: Float) {

        _speechRate.value = rate

    }



    /**
     * Pitch 변경
     */
    fun setPitch(value: Float) {

        _pitch.value = value

    }



    /**
     * TTS 실행
     */
    fun speak() {

        tts.speak(

            text = script.value,

            rate = speechRate.value,

            pitch = pitch.value

        )

    }



    /**
     * Room DB 저장
     */
    fun saveDB() {

        viewModelScope.launch {

            repository.insert(

                ScriptEntity(

                    text = script.value

                )

            )

        }

    }



    /**
     *
     * 신규 소설 샘플 생성 함수
     *
     * 한국소설 3개
     * 미국소설 2개
     *
     * txt + pdf 생성
     *
     */
    fun createSampleNovels() {



        viewModelScope.launch(Dispatchers.IO) {



            val novelList = listOf(

                Pair(
                    "한국소설_1_구름위의약속",
                    "그녀는 구름 위에 앉아 있었다.\n서울의 밤은 조용했고, 그녀의 마음은 더 조용했다."
                ),

                Pair(
                    "한국소설_2_시간의끝",
                    "시간은 끝나지 않는다.\n우리가 끝날 뿐이다."
                ),

                Pair(
                    "한국소설_3_달빛거리",
                    "달빛이 거리를 비췄다.\n그의 그림자는 길게 늘어졌다."
                ),

                Pair(
                    "미국소설_1_The_Last_Promise",
                    "He stood alone in New York.\nThe city never cared."
                ),

                Pair(
                    "미국소설_2_Silent_Road",
                    "The road was silent.\nBut his mind was loud."
                )

            )

            novelList.forEach {

                val fileName = it.first
                val content = it.second

                /**
                 * txt 생성
                 */
                val txtFile =
                    FileUtil.createTxtFile(application, fileName)

                TxtUtil.write(
                    txtFile,
                    content
                )
              /**
                 * pdf 생성
                 */
                val pdfFile =
                    FileUtil.createPdfFile(application, fileName)

                PdfUtil.write(
                    pdfFile,
                    content
                )
         }
        }
    }
    /**
     * 파일 내용 열기
     *
     * txt / pdf 모두 지원
     */
    fun openFile(file: File) {

        viewModelScope.launch(Dispatchers.IO) {

            val content = when {

                file.extension.lowercase() == "txt" -> {

                    TxtUtil.read(file)

                }

                file.extension.lowercase() == "pdf" -> {

                    PdfUtil.read(file)

                }

                else -> ""

            }


            _script.value = content

        }

    }

}