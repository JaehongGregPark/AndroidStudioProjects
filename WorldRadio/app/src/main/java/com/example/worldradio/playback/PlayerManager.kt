package com.example.worldradio.playback

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer

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
    /**
     * ⭐ Media 용 AudioAttributes 설정
     * 볼륨 작게 들리는 문제 해결 핵심
     */
    val player: ExoPlayer =
        ExoPlayer.Builder(context).build().apply {

            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )

            // 기본 볼륨 증폭
            volume = 1.2f
        }


    /**
     * 오디오 포커스
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

        // ⭐ 재생 전 볼륨 다시 설정
        player.volume = 1.3f

        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun setVolume(volume: Float) {
        player.volume = volume
    }

    fun release() {
        player.release()
    }
}