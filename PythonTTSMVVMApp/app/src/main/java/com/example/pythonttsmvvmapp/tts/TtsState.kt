package com.example.pythonttsmvvmapp.tts

/**
 * 현재 TTS 재생 상태
 *
 * UI 는 이 값을 관찰해서
 * "대기", "읽는 중", "일시정지" 를 표시한다.
 */
sealed class TtsState {

    /** 아무것도 하지 않는 상태 */
    object Idle : TtsState()

    /**
     * 음성을 재생 중
     *
     * range = 현재 읽고 있는 텍스트 위치
     */
    data class Speaking(val range: IntRange) : TtsState()

    /** 일시정지 상태 */
    object Paused : TtsState()
}
