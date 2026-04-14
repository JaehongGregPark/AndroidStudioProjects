package com.example.worldradio.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.worldradio.R
import com.example.worldradio.data.model.Country
import com.example.worldradio.databinding.ActivityMainBinding
import com.example.worldradio.playback.RadioService
import com.example.worldradio.ui.adapter.CountryAdapter
import com.example.worldradio.ui.adapter.RadioAdapter
import com.example.worldradio.ui.state.UiState
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private lateinit var adapter: RadioAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()
        setupRecyclerView()
        setupObservers()
        setupCountryGrid()
        observeCountries()

        viewModel.loadCountries()
    }


    /**
     * Tab 설정
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

                    1 -> viewModel.loadFavorites()

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    /**
     * 라디오 목록 RecyclerView
     */
    private fun setupRecyclerView() {

        adapter = RadioAdapter(

            context = this,

            /**
             * ▶ 재생 버튼 클릭
             * RadioService 호출로 변경
             */
            onPlayClick = { station ->

                val url = station.urlResolved

                if (!url.isNullOrEmpty()) {

                    /**
                     * ⭐ RadioService 실행
                     */
                    val intent = Intent(this, RadioService::class.java)
                    intent.putExtra("url", url)
                    intent.putExtra("name", station.name)

                    startForegroundService(intent)

                    /**
                     * ⭐ 미니 플레이어 표시
                     */
                    binding.miniPlayer.visibility = View.VISIBLE
                    binding.tvMiniTitle.text = station.name
                }
            },

            /**
             * 즐겨찾기 클릭
             */
            onFavoriteClick = { station ->

                viewModel.toggleFavorite(station)

            }
        )

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter
    }


    /**
     * ViewModel 상태 관찰
     */
    private fun setupObservers() {

        viewModel.uiState.observe(this) { state ->

            when (state) {

                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is UiState.Success -> {

                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(state.data)
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                }

                else -> {}
            }
        }
    }


    /**
     * 국가 Spinner
     */
    private fun observeCountries() {

        viewModel.countries.observe(this) { countries ->

            val names = countries.map { it.name }

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                names
            )

            binding.spinnerCountry.setAdapter(adapter)

            binding.spinnerCountry.setOnClickListener {
                binding.spinnerCountry.showDropDown()
            }

            binding.spinnerCountry.setOnItemClickListener { _, _, position, _ ->

                val country = countries[position]

                viewModel.searchStations(country.iso_3166_1)

                binding.miniPlayer.visibility = View.GONE
            }
        }
    }


    /**
     * 상단 국가 가로 스크롤
     */
    private fun setupCountryGrid() {

        val countries = getCountryList()

        val adapter = CountryAdapter(countries) { country ->

            viewModel.searchStations(country.iso_3166_1)

            binding.miniPlayer.visibility = View.GONE
        }

        binding.countryRecycler.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.countryRecycler.setHasFixedSize(true)
        binding.countryRecycler.adapter = adapter
    }


    /**
     * 국가 목록
     */
    private fun getCountryList(): List<Country> {

        return listOf(

            Country("Korea", "KR", 0, R.drawable.flag_kr),
            Country("USA", "US", 0, R.drawable.flag_us),
            Country("Japan", "JP", 0, R.drawable.flag_jp),
            Country("UK", "GB", 0, R.drawable.flag_gb),
            Country("Germany", "DE", 0, R.drawable.flag_de),
            Country("France", "FR", 0, R.drawable.flag_fr),
            Country("Canada", "CA", 0, R.drawable.flag_ca),
            Country("Australia", "AU", 0, R.drawable.flag_au),
            Country("Brazil", "BR", 0, R.drawable.flag_br),
            Country("India", "IN", 0, R.drawable.flag_in)

        )
    }
}