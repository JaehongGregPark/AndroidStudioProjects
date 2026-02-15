package com.example.pythonttsmvvmapp.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.pythonttsmvvmapp.tts.TtsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 앱이 꺼져도 음성 재생 유지
 */
@AndroidEntryPoint
class TtsForegroundService : Service() {

    @Inject
    lateinit var ttsManager: TtsManager

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return Notification.Builder(this, "tts_channel")
            .setContentTitle("읽는 중")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }
}