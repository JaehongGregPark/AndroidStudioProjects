package com.example.worldradio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.worldradio.viewmodel.RadioViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import android.widget.Button   // ✅ 추가
import android.widget.EditText
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.worldradio.playback.RadioService
import com.example.worldradio.ui.adapter.RadioAdapter
import kotlin.jvm.java

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: RadioViewModel by viewModels()
    private lateinit var adapter: RadioAdapter
    private lateinit var etCountry: EditText   // 🔥 여기서는 선언만

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.btnSearch)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        etCountry = findViewById(R.id.etCountry)   // 🔥 반드시 여기서 초기화

        adapter = RadioAdapter { station ->

            val intent = Intent(this, RadioService::class.java).apply {
                putExtra("url", station.url)
                putExtra("name", station.name)
            }

            startService(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 🔥 Search 버튼 (하나만!)
        searchButton.setOnClickListener {

            val country = etCountry.text.toString().trim()

            if (country.isNotEmpty()) {
                viewModel.searchStations(country)
            }
        }

        viewModel.stations.observe(this) { stations ->
            Log.d("RADIO", "Loaded: ${stations.size}")
            adapter.submitList(stations)
        }
    }
}