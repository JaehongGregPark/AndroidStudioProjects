package com.example.worldradio.ui

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.worldradio.databinding.ActivityPlayerBinding
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.playback.PlayerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    @Inject
    lateinit var playerManager: PlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name") ?: ""
        val country = intent.getStringExtra("country") ?: ""
        val url = intent.getStringExtra("url") ?: ""

        binding.tvName.text = name
        binding.tvCountry.text = country

        playerManager.play(url)

        binding.btnPlay.setOnClickListener {
            playerManager.resume()
        }

        binding.btnPause.setOnClickListener {
            playerManager.pause()
        }

        binding.seekVolume.max = 100
        binding.seekVolume.progress = 50

        binding.seekVolume.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                playerManager.setVolume(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }
}