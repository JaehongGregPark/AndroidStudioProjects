package com.example.audioscript.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.audioscript.viewmodel.MainViewModel

/**
 * 메인 화면 (Compose UI)
 *
 * 기능:
 * ✔ 텍스트 편집
 * ✔ 번역
 * ✔ 소설 생성
 * ✔ PDF 저장
 * ✔ TTS 속도/피치 조절
 * ✔ 음성 출력
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {

    val text by viewModel.text.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val pitch by viewModel.pitch.collectAsState()

    var storyTitle by remember { mutableStateOf("") }
    var isKorean by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ===============================
        // 텍스트 입력 영역
        // ===============================

        OutlinedTextField(
            value = text,
            onValueChange = { viewModel.updateText(it) },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            label = { Text("텍스트 입력") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ===============================
        // 번역 버튼
        // ===============================

        Button(
            onClick = { viewModel.translate() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🌍 번역")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ===============================
        // 소설 생성 설정
        // ===============================

        OutlinedTextField(
            value = storyTitle,
            onValueChange = { storyTitle = it },
            label = { Text("소설 제목") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("한국어")
            Switch(
                checked = isKorean,
                onCheckedChange = { isKorean = it }
            )
        }

        Button(
            onClick = {
                viewModel.generateStory(storyTitle, isKorean)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("✍ 소설 생성")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ===============================
        // PDF 저장
        // ===============================

        Button(
            onClick = { viewModel.exportPdf("GeneratedStory") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 PDF 저장")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===============================
        // TTS 설정 영역
        // ===============================

        Text("🔊 음성 속도: ${"%.2f".format(speechRate)}")
        Slider(
            value = speechRate,
            onValueChange = { viewModel.setSpeechRate(it) },
            valueRange = 0.5f..2.0f
        )

        Text("🎵 음성 톤: ${"%.2f".format(pitch)}")
        Slider(
            value = pitch,
            onValueChange = { viewModel.setPitch(it) },
            valueRange = 0.5f..2.0f
        )

        Button(
            onClick = { viewModel.speak() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔊 읽기")
        }
    }
}
