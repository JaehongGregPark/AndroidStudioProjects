package com.example.worldradio.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.worldradio.databinding.ActivityMainBinding
import com.example.worldradio.player.RadioPlayer
import com.example.worldradio.ui.adapter.RadioAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 🔥 Hilt로 ViewModel 연결
    private val viewModel: MainViewModel by viewModels()

    private lateinit var adapter: RadioAdapter
    private lateinit var player: RadioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = RadioPlayer(this)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    // ===============================
    // RecyclerView 설정
    // ===============================
    private fun setupRecyclerView() {

        adapter = RadioAdapter(this) { station ->

            // 🔥 아이템 클릭 시 재생
            player.play(station.url)

            binding.miniPlayer.visibility = View.VISIBLE
            binding.tvMiniTitle.text = station.name
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    // ===============================
    // LiveData 옵저버
    // ===============================
    private fun setupObservers() {

        viewModel.stations.observe(this) { list ->

            adapter.submitList(list)
            binding.progressBar.visibility = View.GONE
        }
    }

    // ===============================
    // 버튼 클릭 리스너
    // ===============================
    private fun setupClickListeners() {

        // 🔍 검색 버튼
        binding.btnSearch.setOnClickListener {

            val country = binding.etCountry.text.toString()

            if (country.isNotEmpty()) {

                binding.progressBar.visibility = View.VISIBLE
                viewModel.searchStations(country)
            }
        }

        // ⏸ 미니 플레이어 정지
        binding.btnPause.setOnClickListener {

            player.stop()
            binding.miniPlayer.visibility = View.GONE
        }
    }
}