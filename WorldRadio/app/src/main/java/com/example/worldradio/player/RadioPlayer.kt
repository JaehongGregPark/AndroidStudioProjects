package com.example.worldradio.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class RadioPlayer(context: Context) {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    fun play(url: String) {

        val mediaItem = MediaItem.fromUri(url)

        player.setMediaItem(mediaItem)

        player.prepare()

        // ⭐ 기본보다 약간 더 크게
        player.volume = 1.5f

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