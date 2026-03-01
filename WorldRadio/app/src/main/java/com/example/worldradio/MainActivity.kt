package com.example.worldradio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.worldradio.viewmodel.RadioViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import android.widget.Button   // ✅ 추가
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.btnSearch)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // 🔥 Adapter 생성 (클릭 시 동작 정의)
        adapter = RadioAdapter { station ->

            val intent = Intent(this, RadioService::class.java).apply {
                putExtra("url", station.url)
                putExtra("name", station.name)
            }

            startService(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchButton.setOnClickListener {
            viewModel.searchStations()
        }

        viewModel.stations.observe(this) { stations ->
            Log.d("RADIO", "Loaded: ${stations.size}")
            adapter.submitList(stations)   // 🔥 이 줄이 핵심
        }
    }
}