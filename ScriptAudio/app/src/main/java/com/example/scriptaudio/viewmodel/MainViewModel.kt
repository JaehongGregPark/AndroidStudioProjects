package com.example.scriptaudio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.scriptaudio.data.local.ScriptEntity
import com.example.scriptaudio.data.local.ScriptRepository
import com.example.scriptaudio.tts.TTSManager

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.launch

import javax.inject.Inject


/**
 *
 * MainViewModel
 *
 * 앱의 핵심 로직 담당
 *
 * 기능:
 *
 * ✔ 텍스트 상태 관리
 * ✔ TTS 실행
 * ✔ TTS 속도 조절
 * ✔ TTS Pitch 조절
 * ✔ Room DB 저장
 *
 */

@HiltViewModel
class MainViewModel @Inject constructor(

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
     *
     * 텍스트 변경
     *
     */
    fun updateScript(text: String) {

        _script.value = text

    }



    /**
     *
     * 속도 변경
     *
     */
    fun setSpeechRate(rate: Float) {

        _speechRate.value = rate

    }



    /**
     *
     * Pitch 변경
     *
     */
    fun setPitch(value: Float) {

        _pitch.value = value

    }



    /**
     *
     * TTS 실행
     *
     */
    fun speak() {

        tts.speak(

            text = script.value,

            rate = speechRate.value,

            pitch = pitch.value

        )

    }



    /**
     *
     * Room DB 저장
     *
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

}