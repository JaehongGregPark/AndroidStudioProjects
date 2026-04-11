package com.example.worldradio.playback

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.worldradio.R
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper


/**
 * MediaStyle 알림 생성 클래스
 *
 * MediaSession을 연결하여
 * 재생/일시정지 버튼 자동 연동
 */
object NotificationHelper {

    fun createNotification(
        context: Context,
        mediaSession: MediaSession,
        title: String
    ): Notification {

        return NotificationCompat.Builder(context, "radio_channel")
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_radio)
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(mediaSession)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
}