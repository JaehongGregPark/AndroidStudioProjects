package com.example.worldradio.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.worldradio.R
import com.example.worldradio.data.model.Country
import com.example.worldradio.databinding.ActivityMainBinding
import com.example.worldradio.player.RadioPlayer
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

    private lateinit var player: RadioPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = RadioPlayer(this)

        setupTabs()
        setupRecyclerView()
        setupObservers()
        setupCountryGrid()
        observeCountries()

        /** ⭐ 전체 국가 로드 */
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

            onPlayClick = { station ->

                val url = station.urlResolved

                if (!url.isNullOrEmpty()) {

                    player.play(url)

                    binding.miniPlayer.visibility = View.VISIBLE

                    binding.tvMiniTitle.text = station.name
                }
            },

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
     * ⭐ RadioBrowser 전체 국가 → Spinner 연결
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
     * 국가 Grid (빠른 선택)
     */
    private fun setupCountryGrid() {

        val countries = getCountryList()

        val adapter = CountryAdapter(countries) { country ->

            viewModel.searchStations(country.iso_3166_1)

            binding.miniPlayer.visibility = View.GONE
        }

        binding.countryRecycler.layoutManager =
            GridLayoutManager(this, 5)

        binding.countryRecycler.adapter = adapter
    }


    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }


    /**
     * 상단 빠른 선택 국가
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