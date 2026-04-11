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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.scriptaudio.navigation.NavGraph
import com.example.scriptaudio.ui.theme.AppTheme
import com.example.scriptaudio.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {

            val viewModel: MainViewModel = hiltViewModel()
            val dark by viewModel.darkMode.collectAsState()

            val navController = rememberNavController()

            val followSystem by viewModel.followSystem.collectAsState()
            val amoled by viewModel.amoledBlack.collectAsState()
            val themeColor by viewModel.themeColor.collectAsState()

            val systemDark = isSystemInDarkTheme()

            val useDark = if (followSystem) systemDark else dark

            val colorScheme = when {

                amoled && useDark ->
                    darkColorScheme(
                        background = Color.Black,
                        surface = Color.Black
                    )

                useDark ->
                    darkColorScheme()

                else ->
                    lightColorScheme()
            }

            AppTheme(
                darkMode = dark,
                followSystem = followSystem,
                amoled = amoled,
                themeColor = themeColor
            ) {
                NavGraph(navController = navController)
            }
       


        }

    }

}