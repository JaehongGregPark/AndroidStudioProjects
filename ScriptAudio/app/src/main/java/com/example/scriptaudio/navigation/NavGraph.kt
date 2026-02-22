package com.example.scriptaudio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.scriptaudio.ui.main.MainScreen
import com.example.scriptaudio.ui.settings.SettingsScreen

/**
 * 앱 전체 Navigation 관리
 */
@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(

        navController = navController,

        startDestination = "main"

    ) {

        /**
         * 메인 화면
         */
        composable("main") {

            MainScreen(

                onSettingsClick = {

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