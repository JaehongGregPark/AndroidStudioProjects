package com.example.pythonttsmvvmapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.pythonttsmvvmapp.service.TtsForegroundService
import com.example.pythonttsmvvmapp.viewmodel.ReaderViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱 메인 화면
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Hilt 자동 ViewModel 생성
    private val viewModel: ReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 백그라운드 TTS 시작
        startService(Intent(this, TtsForegroundService::class.java))
    }
}