package com.example.worldradio.playback

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.media3.common.MediaItem

/**
 * ExoPlayer 관리 전담 클래스
 *
 * Service에서 직접 Player를 다루지 않고
 * 책임을 분리하여 유지보수성 향상
 */
@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    /**
     * 오디오 포커스 요청
     * 다른 음악 앱과 충돌 방지
     */
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val focusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener { change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS -> player.pause()
                    AudioManager.AUDIOFOCUS_GAIN -> player.play()
                }
            }
            .build()

    fun play(url: String) {
        audioManager.requestAudioFocus(focusRequest)

        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun release() {
        player.release()
        audioManager.abandonAudioFocusRequest(focusRequest)
    }
}