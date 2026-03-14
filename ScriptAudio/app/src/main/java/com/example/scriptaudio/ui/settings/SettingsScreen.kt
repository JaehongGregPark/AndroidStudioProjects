package com.example.scriptaudio.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    ttsSpeed: Float,
    fontSize: Float,
    scrollSpeed: Float,
    onTtsSpeedChange: (Float) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onScrollSpeedChange: (Float) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reader Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("TTS Speed: ${String.format("%.1f", ttsSpeed)}x")
                Slider(
                    value = ttsSpeed,
                    onValueChange = onTtsSpeedChange,
                    valueRange = 0.5f..2.0f
                )
            }

            item {
                Text("Font Size: ${fontSize.toInt()}")
                Slider(
                    value = fontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 12f..40f
                )
            }

            item {
                Text("Auto Scroll Speed: ${String.format("%.1f", scrollSpeed)}x")
                Slider(
                    value = scrollSpeed,
                    onValueChange = onScrollSpeedChange,
                    valueRange = 0.5f..5f
                )
            }
        }
    }
}