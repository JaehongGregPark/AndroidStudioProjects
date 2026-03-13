package com.example.scriptaudio.navigation

// Compose UI
import androidx.compose.runtime.Composable

// Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Hilt ViewModel
import androidx.hilt.navigation.compose.hiltViewModel

// Screens
import com.example.scriptaudio.ui.reader.ReaderScreen
import com.example.scriptaudio.ui.settings.SettingsScreen

// ViewModel
import com.example.scriptaudio.viewmodel.MainViewModel

/**
 * NavGraph
 *
 * 앱의 모든 화면 이동 관리
 *
 * ReaderScreen
 * SettingsScreen
 */
@Composable
fun NavGraph() {

    /**
     * Navigation Controller
     *
     * 화면 이동을 관리하는 객체
     */
    val navController = rememberNavController()

    /**
     * NavHost
     *
     * 시작 화면 + 화면 경로 등록
     */
    NavHost(

        navController = navController,

        /**
         * 앱 시작 화면
         */
        startDestination = "reader"

    ) {

        /**
         * Reader 메인 화면
         */
        composable("reader") {

            /**
             * Hilt ViewModel 생성
             */
            val viewModel: MainViewModel = hiltViewModel()

            ReaderScreen(

                viewModel = viewModel,

                onOpenSettings = {

                    navController.navigate("settings")

                }

            )
        }

        /**
         * 설정 화면
         */
        composable("settings") {

            SettingsScreen(

                onBackClick = {

                    navController.popBackStack()

                }

            )

        }

    }

}