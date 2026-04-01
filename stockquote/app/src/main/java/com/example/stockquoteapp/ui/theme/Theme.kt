package com.example.stockquoteapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MintGreen,
    secondary = SoftGreen,
    tertiary = Crimson,
    background = WarmGray,
    surface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF0ECE7),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = Slate,
    onBackground = Slate,
    onSurface = Slate,
    onSurfaceVariant = MutedText
)

private val DarkColors = darkColorScheme(
    primary = SoftGreen,
    secondary = MintGreen,
    tertiary = Crimson
)

@Composable
fun StockQuoteAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
