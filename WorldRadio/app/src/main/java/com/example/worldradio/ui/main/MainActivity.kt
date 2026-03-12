package com.example.worldradio.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.worldradio.databinding.ActivityMainBinding
import com.example.worldradio.player.RadioPlayer
import com.example.worldradio.ui.adapter.RadioAdapter
import com.example.worldradio.ui.state.UiState
import com.example.worldradio.util.CountryUtil
import dagger.hilt.android.AndroidEntryPoint

/**
 * 메인 화면
 *
 * 흐름
 *
 * 국가 입력
 * ↓
 * 검색 버튼
 * ↓
 * 방송국 리스트 표시
 * ↓
 * Play 버튼 클릭
 * ↓
 * 라디오 재생
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private lateinit var adapter: RadioAdapter

    private lateinit var player: RadioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        player = RadioPlayer(this)

        setupAutoComplete()

        setupRecyclerView()

        setupObservers()

        setupSearch()
    }

    /**
     * 국가 자동완성
     */
    private fun setupAutoComplete() {

        val countries = CountryUtil.getAllCountries()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            countries
        )

        binding.etCountry.setAdapter(adapter)
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {

        //adapter = RadioAdapter(this) { station ->

            // Play 클릭 → 라디오 재생
          //  player.play(station.url)
        //}

        adapter = RadioAdapter(this) { station ->

            player.play(station.url)

            binding.miniPlayer.visibility = View.VISIBLE

            binding.tvMiniTitle.text = station.name
        }

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter
    }

    /**
     * 검색 버튼
     */
    private fun setupSearch() {

        binding.btnSearch.setOnClickListener {

            val country =
                binding.etCountry.text.toString()

            viewModel.searchStations(country)
        }
    }

    /**
     * ViewModel 상태 관찰
     */
    private fun setupObservers() {

        viewModel.uiState.observe(this) { state ->

            when (state) {

                is UiState.Loading -> {

                    binding.progressBar.visibility = android.view.View.VISIBLE
                }

                is UiState.Success -> {

                    binding.progressBar.visibility = android.view.View.GONE

                    adapter.submitList(state.data)
                }

                is UiState.Error -> {

                    binding.progressBar.visibility = android.view.View.GONE
                }

                else -> {}
            }
        }
    }
}