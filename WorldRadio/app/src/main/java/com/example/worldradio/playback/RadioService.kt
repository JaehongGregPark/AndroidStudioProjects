package com.example.worldradio.playback

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.common.MediaItem
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

/**
 * Foreground Service
 *
 * 앱이 백그라운드에 있어도
 * 음악이 중단되지 않도록 유지
 */
@AndroidEntryPoint
class RadioService : Service() {

    @Inject
    lateinit var playerManager: PlayerManager

    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()

        /**
         * MediaSession 생성
         * 잠금화면/블루투스/자동차 연동
         */
        mediaSession = MediaSession.Builder(
            this,
            playerManager.player
        ).build()

        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val name = intent.getStringExtra("name") ?: "Radio"

        playerManager.play(url)

        val notification =
            NotificationHelper.createNotification(
                this,
                mediaSession,
                name
            )

        startForeground(1, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        playerManager.release()
        mediaSession.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    /**
     * Android 8+ 필수
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "radio_channel",
            "Radio Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}