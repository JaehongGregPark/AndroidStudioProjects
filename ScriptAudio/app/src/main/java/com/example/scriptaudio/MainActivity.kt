package com.example.scriptaudio

/**
 * ScriptAudio MainActivity
 *
 * 앱 진입점
 * Compose UI 실행
 */

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.scriptaudio.navigation.NavGraph
import com.example.scriptaudio.viewmodel.MainViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            /**
             * ViewModel 생성
             */
    // Hilt 주입 사용
            val viewModel: MainViewModel = hiltViewModel()
            //val viewModel: MainViewModel = viewModel()

            /**
             * Navigation 시작
             */

            NavGraph()

        }

    }

}