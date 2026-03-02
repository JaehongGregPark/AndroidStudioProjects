package com.example.worldradio.player

import android.content.Context
import android.media.MediaPlayer

// 라디오 스트리밍 전용 플레이어 클래스
// Activity와 분리하여 재사용 가능하게 설계
class RadioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    // 현재 재생 중 URL
    var currentUrl: String? = null
        private set

    // 라디오 재생
    fun play(url: String) {

        stop() // 기존 재생 중지

        mediaPlayer = MediaPlayer().apply {

            setDataSource(url)      // 스트림 URL 설정
            prepareAsync()          // 비동기 준비

            setOnPreparedListener {
                start()             // 준비 완료 시 자동 재생
            }
        }

        currentUrl = url
    }

    // 재생 중지
    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentUrl = null
    }
}