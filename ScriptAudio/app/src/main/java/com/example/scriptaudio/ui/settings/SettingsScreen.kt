package com.example.scriptaudio.ui.settings

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scriptaudio.viewmodel.MainViewModel

/**
 * 설정 화면
 *
 * 기능:
 * ✔ TTS 속도 조절
 * ✔ Pitch 조절
 * ✔ 뒤로가기
 */
@Composable
fun SettingsScreen(

    onBackClick: () -> Unit,   // ← 이것 추가 ★

    viewModel: MainViewModel = hiltViewModel()

) {

    val rate by viewModel.speechRate.collectAsState()
    val pitch by viewModel.pitch.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        Text("설정", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onBackClick) {

            Text("뒤로가기")

        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("속도")

        Slider(
            value = rate,
            onValueChange = {
                viewModel.speechRate.value = it
            },
            valueRange = 0.5f..2f
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Pitch")

        Slider(
            value = pitch,
            onValueChange = {
                viewModel.pitch.value = it
            },
            valueRange = 0.5f..2f
        )

    }

}