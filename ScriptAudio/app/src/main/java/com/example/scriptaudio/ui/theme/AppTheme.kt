package com.example.scriptaudio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 앱 전체 Theme 관리
 *
 * 기능
 * - dark mode
 * - system follow
 * - AMOLED black
 * - theme color 선택
 */
@Composable
fun AppTheme(
    darkMode: Boolean,
    followSystem: Boolean,
    amoled: Boolean,
    themeColor: String,
    content: @Composable () -> Unit
) {

    val systemDark = isSystemInDarkTheme()
    val useDark = if (followSystem) systemDark else darkMode

    val primary = when (themeColor) {
        "green" -> Color(0xFF4CAF50)
        "purple" -> Color(0xFF9C27B0)
        "orange" -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }

    val colorScheme = when {

        // AMOLED black
        amoled && useDark ->
            darkColorScheme(
                primary = primary,
                background = Color.Black,
                surface = Color.Black
            )

        // Dark mode
        useDark ->
            darkColorScheme(
                primary = primary
            )

        // Light mode
        else ->
            lightColorScheme(
                primary = primary
            )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}