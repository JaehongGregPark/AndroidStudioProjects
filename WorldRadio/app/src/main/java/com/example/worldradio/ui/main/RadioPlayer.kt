package com.example.worldradio.ui.main

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

object RadioPlayer {

    private var player: ExoPlayer? = null

    fun play(context: Context, url: String) {

        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }

        val mediaItem = MediaItem.fromUri(url)

        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    fun stop() {
        player?.stop()
    }
}