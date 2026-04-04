package com.example.moviecatalog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = StormBlue,
    onPrimary = Cloud,
    secondary = GoldGlow,
    onSecondary = MidnightNavy,
    background = Cloud,
    onBackground = MidnightNavy,
    surface = Color.White,
    onSurface = MidnightNavy,
    onSurfaceVariant = Slate,
    error = Coral
)

private val DarkColors = darkColorScheme(
    primary = GoldGlow,
    onPrimary = MidnightNavy,
    secondary = StormBlue,
    onSecondary = Cloud,
    background = MidnightNavy,
    onBackground = Cloud,
    surface = StormBlue,
    onSurface = Cloud,
    onSurfaceVariant = Color(0xFFB8C0CC),
    error = Coral
)

@Composable
fun MovieCatalogTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
