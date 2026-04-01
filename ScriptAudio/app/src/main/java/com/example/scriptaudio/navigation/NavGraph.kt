package com.example.scriptaudio.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.scriptaudio.ui.main.MainScreen
import com.example.scriptaudio.ui.settings.SettingsScreen
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

   /* 네비게이션 컨트롤러 생성
   *
   * - Compose Navigation의 핵심 객체
   * - 화면 이동(navController.navigate) 담당
   */

    val navController = rememberNavController()

    NavHost(navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onReaderClick = { navController.navigate("reader") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("reader") {
            ReaderScreen(viewModel)
        }

        /**
        * =========================
        * 설정 화면 (신규 추가)
        * =========================
        */
        composable("settings") {
        /**
        * SettingsScreen 호출
        *
        * 필요하면:
        * - viewModel 전달 가능
        * - navController 전달해서 뒤로가기 구현 가능
        */
                  SettingsScreen()
        }
    }
}