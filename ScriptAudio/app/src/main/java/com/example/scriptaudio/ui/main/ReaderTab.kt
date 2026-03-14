package com.example.scriptaudio.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ReaderTab
 *
 * - MainScreen 내 Reader 탭 UI
 * - 원문 텍스트를 읽고, 편집 가능
 * - TTS 버튼으로 MainViewModel의 speak() 실행
 *
 * @param text 원문 텍스트
 * @param onTextChange 텍스트 변경 콜백
 * @param onSpeak TTS 실행 콜백
 */
@Composable
fun ReaderTab(
    text: String,
    onTextChange: (String) -> Unit,
    onSpeak: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Reader", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        // 원문 텍스트 입력 필드
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // 남은 공간 모두 사용
            maxLines = 10000
        )

        Spacer(Modifier.height(10.dp))

        // TTS 읽기 버튼
        Button(
            onClick = onSpeak,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("TTS 읽기")
        }
    }
}