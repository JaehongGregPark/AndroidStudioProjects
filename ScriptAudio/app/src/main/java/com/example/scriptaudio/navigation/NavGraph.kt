package com.example.scriptaudio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.scriptaudio.ui.main.MainScreen
import com.example.scriptaudio.ui.settings.SettingsScreen

@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(

        navController = navController,
        startDestination = "main"

    ) {

        composable("main") {

            MainScreen(

                onSettingsClick = {

                    navController.navigate("settings")

                }

            )

        }

        composable("settings") {

            SettingsScreen(

                onBackClick = {

                    navController.popBackStack()

                }

            )

        }

    }

}