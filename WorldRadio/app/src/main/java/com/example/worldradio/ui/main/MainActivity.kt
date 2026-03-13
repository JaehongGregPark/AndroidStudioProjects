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
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // ViewBinding
    private lateinit var binding: ActivityMainBinding

    // ViewModel
    private val viewModel: MainViewModel by viewModels()

    // RecyclerView Adapter
    private lateinit var adapter: RadioAdapter

    // ⭐ 라디오 스트리밍 플레이어
    private lateinit var player: RadioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ⭐ ExoPlayer 기반 라디오 플레이어 생성
        player = RadioPlayer(this)

        // 각 기능 초기화
        setupTabs()
        setupAutoComplete()
        setupRecyclerView()
        setupObservers()
        setupSearch()
    }

    /**
     * ⭐ 상단 탭 설정
     *
     * 0 : Radio 검색
     * 1 : Favorites (즐겨찾기)
     */
    private fun setupTabs() {

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Radio")
        )

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Favorites")
        )

        binding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {

                when (tab?.position) {

                    // ⭐ Radio 검색 탭
                    0 -> {

                        val country = binding.etCountry.text.toString()

                        if (country.isNotEmpty()) {
                            viewModel.searchStations(country)
                        }
                    }

                    // ⭐ 즐겨찾기 탭
                    1 -> {
                        viewModel.loadFavorites()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /**
     * ⭐ 국가 자동완성 설정
     *
     * WorldRadio API 지원 국가 목록을
     * AutoCompleteTextView에 연결
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
     * ⭐ RecyclerView 설정
     *
     * - 방송 클릭 → 라디오 재생
     * - 별 클릭 → 즐겨찾기 추가
     */
    private fun setupRecyclerView() {

        adapter = RadioAdapter(

            context = this,

            // ▶ 라디오 재생
            onPlayClick = { station ->

                val url = station.urlResolved

                if (!url.isNullOrEmpty()) {

                    player.play(url)

                    binding.miniPlayer.visibility = View.VISIBLE
                    binding.tvMiniTitle.text = station.name
                }
            },

            // ⭐ 즐겨찾기 추가
            onFavoriteClick = { station ->

                viewModel.toggleFavorite(station)

            }
        )

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter
    }

    /**
     * ⭐ 검색 버튼 처리
     *
     * 입력된 국가 기준으로
     * RadioBrowser API 검색
     */
    private fun setupSearch() {

        binding.btnSearch.setOnClickListener {

            val country = binding.etCountry.text.toString()

            if (country.isNotEmpty()) {

                viewModel.searchStations(country)

            }
        }
    }

    /**
     * ⭐ ViewModel 상태 관찰
     *
     * Loading
     * Success
     * Error
     */
    private fun setupObservers() {

        viewModel.uiState.observe(this) { state ->

            when (state) {

                // 로딩 표시
                is UiState.Loading -> {

                    binding.progressBar.visibility = View.VISIBLE
                }

                // 데이터 로드 성공
                is UiState.Success -> {

                    binding.progressBar.visibility = View.GONE

                    // RecyclerView 업데이트
                    adapter.submitList(state.data)
                }

                // 에러 발생
                is UiState.Error -> {

                    binding.progressBar.visibility = View.GONE
                }

                else -> {}
            }
        }
    }

    /**
     * ⭐ Activity 종료 시 플레이어 해제
     */
    override fun onDestroy() {

        super.onDestroy()

        player.release()
    }
}