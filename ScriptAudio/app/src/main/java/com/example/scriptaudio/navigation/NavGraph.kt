package com.example.scriptaudio.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scriptaudio.ui.main.MainScreen
import com.example.scriptaudio.ui.reader.ReaderScreen
import com.example.scriptaudio.viewmodel.MainViewModel

/**
 * NavGraph
 *
 * - 앱 화면 이동 관리
 * - "main" → MainScreen
 * - "reader" → ReaderScreen
 */
@Composable
fun NavGraph(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onReaderClick = { navController.navigate("reader") },
                onSettingsClick = { /* TODO: 설정 화면 */ }
            )
        }
        composable("reader") {
            ReaderScreen(viewModel)
        }
    }
}