package com.example.worldradio.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.worldradio.data.model.Country
import com.example.worldradio.databinding.ActivityMainBinding
import com.example.worldradio.player.RadioPlayer
import com.example.worldradio.ui.adapter.CountryAdapter
import com.example.worldradio.ui.adapter.RadioAdapter
import com.example.worldradio.ui.state.UiState
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import com.example.worldradio.R

/**
 * 메인 화면 Activity
 *
 * 기능
 * 1️⃣ 국가 선택 (Spinner / 아이콘)
 * 2️⃣ 라디오 방송 목록 표시
 * 3️⃣ 방송 클릭 → 라디오 재생
 * 4️⃣ 즐겨찾기 추가
 * 5️⃣ Mini Player 표시
 */

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /** ViewBinding */
    private lateinit var binding: ActivityMainBinding

    /** ViewModel (MVVM 구조) */
    private val viewModel: MainViewModel by viewModels()

    /** RecyclerView Adapter */
    private lateinit var adapter: RadioAdapter

    /** 라디오 스트리밍 플레이어 (ExoPlayer 기반) */
    private lateinit var player: RadioPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** ViewBinding 초기화 */
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** 라디오 플레이어 생성 */
        player = RadioPlayer(this)

        /** UI 기능 초기화 */
        setupTabs()
        setupRecyclerView()
        setupObservers()
        setupCountrySpinner()
        setupCountryGrid()
        //setupCountryIcons()
    }


    /**
     * 상단 Tab 설정
     *
     * Radio
     * Favorites
     */
    private fun setupTabs() {

        // Radio 탭
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Radio")
        )

        // Favorites 탭
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText("Favorites")
        )

        // 탭 선택 이벤트
        binding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {

                when (tab?.position) {

                    // Favorites 탭 선택
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
     * RecyclerView 설정
     *
     * 라디오 방송 리스트 표시
     */
    private fun setupRecyclerView() {

        adapter = RadioAdapter(

            context = this,

            /**
             * 방송 클릭 → 라디오 재생
             */
            onPlayClick = { station ->

                val url = station.urlResolved

                if (!url.isNullOrEmpty()) {

                    // 스트리밍 시작
                    player.play(url)

                    // Mini Player 표시
                    binding.miniPlayer.visibility = View.VISIBLE

                    // 현재 방송 이름 표시
                    binding.tvMiniTitle.text = station.name
                }
            },

            /**
             * 즐겨찾기 버튼 클릭
             */
            onFavoriteClick = { station ->

                viewModel.toggleFavorite(station)

            }
        )

        // 세로 리스트
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter
    }


    /**
     * ViewModel 상태 관찰
     *
     * Loading
     * Success
     * Error
     */
    private fun setupObservers() {

        viewModel.uiState.observe(this) { state ->

            when (state) {

                /** 데이터 로딩 중 */
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                /** 데이터 로드 성공 */
                is UiState.Success -> {

                    binding.progressBar.visibility = View.GONE

                    // RecyclerView 업데이트
                    adapter.submitList(state.data)
                }

                /** 오류 발생 */
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                }

                else -> {}
            }
        }
    }


    /**
     * 국가 Spinner 설정
     *
     * 국가 이름 + 코드 매핑
     *
     * 형식
     * "Country Name|CODE"
     */
    private fun setupCountrySpinner() {

        val countries = listOf(
            "South Korea|KR",
            "United States|US",
            "Japan|JP",
            "United Kingdom|GB",
            "Germany|DE",
            "France|FR",
            "Canada|CA",
            "Australia|AU",
            "Brazil|BR",
            "India|IN"
        )

        /** Spinner에 표시할 국가 이름만 추출 */
        val names = countries.map { it.split('|')[0] }

        /** Spinner Adapter */
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            names
        )

        //binding.spinnerCountry.adapter = spinnerAdapter
        binding.spinnerCountry.setAdapter(spinnerAdapter)

        /**
         * Spinner 선택 이벤트
         */
        binding.spinnerCountry.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    // 국가 코드 추출
                    val code = countries[position].split('|')[1]

                    // 방송 검색
                    viewModel.searchStations(code)
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }
    }

    private fun setupCountryGrid() {

        val countries = getCountryList()

        val adapter = CountryAdapter(countries) { country ->

            // 클릭 시 라디오 검색
            viewModel.searchStations(country.iso_3166_1)

            // MiniPlayer 숨김
            binding.miniPlayer.visibility = View.GONE
        }

        binding.countryRecycler.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(this, 5)

        binding.countryRecycler.adapter = adapter
    }
    /**
     * 국가 아이콘 클릭 처리
     *
     * 아이콘 클릭 → Spinner 선택 변경
     * Spinner → 자동 검색 실행
     */
    /*
    private fun setupCountryIcons() {

        binding.flag_kr.setOnClickListener {
            binding.spinnerCountry.setSelection(0)
        }

        binding.flag_us.setOnClickListener {
            binding.spinnerCountry.setSelection(1)
        }

        binding.flag_jp.setOnClickListener {
            binding.spinnerCountry.setSelection(2)
        }

        binding.flag_gb.setOnClickListener {
            binding.spinnerCountry.setSelection(3)
        }

        binding.flag_de.setOnClickListener {
            binding.spinnerCountry.setSelection(4)
        }
    }
*/

    /**
     * Activity 종료 시
     * 플레이어 메모리 해제
     */
    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun getCountryList(): List<Country> {

        return listOf(

            Country("Korea", "KR", 0,R.drawable.flag_kr),
            Country("USA", "US", 0,R.drawable.flag_us),
            Country("Japan", "JP", 0,R.drawable.flag_jp),
            Country("UK", "GB", 0,R.drawable.flag_gb),
            Country("Germany", "DE", 0,R.drawable.flag_de),
            Country("France", "FR", 0,R.drawable.flag_fr),
            Country("Canada", "CA", 0,R.drawable.flag_ca),
            Country("Australia", "AU", 0,R.drawable.flag_au),
            Country("Brazil", "BR", 0,R.drawable.flag_br),
            Country("India", "IN", 0,R.drawable.flag_in)

        )
    }
}