package com.example.worldradio.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class RadioPlayer(context: Context) {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    init {

        // ⭐ 스트림 끊김 자동 재연결
        player.addListener(object : Player.Listener {

            override fun onPlayerError(error: PlaybackException) {

                player.stop()
                player.prepare()
                player.play()
            }
        })
    }

    fun play(url: String) {

        val mediaItem = MediaItem.fromUri(url)

        player.setMediaItem(mediaItem)

        player.prepare()

        player.volume = 1.0f

        player.play()
    }

    fun stop() {
        player.stop()
    }

    fun release() {
        player.release()
    }

    fun setVolume(value: Float) {
        player.volume = value
    }
}