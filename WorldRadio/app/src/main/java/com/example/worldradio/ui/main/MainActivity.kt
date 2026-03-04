package com.example.worldradio.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.worldradio.databinding.ActivityMainBinding
import com.example.worldradio.player.RadioPlayer
import com.example.worldradio.ui.adapter.RadioAdapter
import com.example.worldradio.ui.state.UiState
import com.example.worldradio.util.CountryUtil
import com.example.worldradio.util.RecentSearchManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * 메인 화면 Activity
 *
 * 기능:
 * 1️⃣ 국가 검색
 * 2️⃣ 인기 국가 버튼
 * 3️⃣ 자동완성
 * 4️⃣ 최근 검색 저장
 * 5️⃣ 로딩 / 에러 상태 처리
 * 6️⃣ 라디오 재생 (Mini Player)
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Hilt로 ViewModel 주입
    private val viewModel: MainViewModel by viewModels()

    private lateinit var adapter: RadioAdapter
    private lateinit var player: RadioPlayer
    private lateinit var recentSearchManager: RecentSearchManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 라디오 플레이어 초기화
        player = RadioPlayer(this)

        // 최근 검색 관리자 초기화
        recentSearchManager = RecentSearchManager(this)
        val countries = CountryUtil.getAllCountries()

        val adapterAuto =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                countries
            )

        binding.etCountry.setAdapter(adapterAuto)

        setupRecyclerView()
        setupAutoComplete()
        setupObservers()
        setupClickListeners()
        setupPopularButtons()
    }

    // =====================================================
    // 1️⃣ RecyclerView 설정
    // =====================================================
    private fun setupRecyclerView() {

        adapter = RadioAdapter(this) { station ->

            // 방송국 클릭 시 재생
            player.play(station.url)

            // Mini Player UI 표시
            binding.miniPlayer.visibility = View.VISIBLE
            binding.tvMiniTitle.text = station.name
        }

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter
    }

    // =====================================================
    // 2️⃣ 자동완성 설정
    // =====================================================
    private fun setupAutoComplete() {

        // 기본 국가 리스트 + 최근 검색 추가
        val defaultCountries = listOf(
            "Korea",
            "South Korea",
            "Japan",
            "United States",
            "Germany",
            "France",
            "United Kingdom"
        )

        val recent = recentSearchManager.getRecent()

        val combined = (recent + defaultCountries).distinct()

        val autoAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            combined
        )

        binding.etCountry.setAdapter(autoAdapter)
    }

    // =====================================================
    // 3️⃣ LiveData 관찰 (UiState 패턴)
    // =====================================================
    private fun setupObservers() {

        viewModel.uiState.observe(this) { state ->

            when (state) {

                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE

                    adapter.submitList(state.data)
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }

                else -> Unit
            }
        }
    }

    // =====================================================
    // 4️⃣ 버튼 클릭 리스너
    // =====================================================
    private fun setupClickListeners() {

        // 🔍 검색 버튼
        binding.btnSearch.setOnClickListener {

            val country =
                binding.etCountry.text.toString()

            if (country.isNotEmpty()) {

                // 최근 검색 저장
                recentSearchManager.saveCountry(country)

                viewModel.searchStations(country)
            }
        }

        // ⏸ Mini Player 정지 버튼
        binding.btnPause.setOnClickListener {

            player.stop()
            binding.miniPlayer.visibility = View.GONE
        }
    }

    // =====================================================
    // 5️⃣ 인기 국가 버튼 설정
    // =====================================================
    private fun setupPopularButtons() {

        binding.btnKorea.setOnClickListener {
            searchFromButton("Korea")
        }

        binding.btnUSA.setOnClickListener {
            searchFromButton("United States")
        }

        binding.btnJapan.setOnClickListener {
            searchFromButton("Japan")
        }
    }

    /**
     * 인기 국가 버튼 클릭 공통 처리
     */
    private fun searchFromButton(country: String) {

        // 입력창에 표시
        binding.etCountry.setText(country)

        // 최근 검색 저장
        recentSearchManager.saveCountry(country)

        // 검색 실행
        viewModel.searchStations(country)
    }

    // =====================================================
    // 6️⃣ Activity 종료 시 플레이어 해제
    // =====================================================
    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }
}