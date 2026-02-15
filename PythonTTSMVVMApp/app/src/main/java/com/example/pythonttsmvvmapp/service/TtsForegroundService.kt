package com.example.pythonttsmvvmapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.pythonttsmvvmapp.tts.TtsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 앱이 꺼져도 TTS 음성 재생 유지
 * Hilt로 TtsManager 안전하게 주입
 */
@AndroidEntryPoint
class TtsForegroundService : Service() {

    // Hilt로 주입
    @Inject
    lateinit var ttsManager: TtsManager


    override fun onCreate() {
        super.onCreate()

        startForeground(1, createNotification())

        if (::ttsManager.isInitialized) {
            ttsManager.speak("앱이 시작되었습니다")  // 실제 존재하는 메서드 이름 사용
        } else {
            android.util.Log.e("TtsService", "ttsManager not initialized!")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "tts_channel"
        val channelName = "TTS Service"

        // Android 8.0+에서는 NotificationChannel 필수
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("읽는 중")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }
}
