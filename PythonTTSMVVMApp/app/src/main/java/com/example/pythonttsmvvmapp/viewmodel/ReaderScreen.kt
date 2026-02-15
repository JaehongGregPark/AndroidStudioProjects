package com.example.pythonttsmvvmapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.example.pythonttsmvvmapp.tts.TtsState
import com.example.pythonttsmvvmapp.viewmodel.ReaderViewModel

@Composable
fun ReaderScreen(viewModel: ReaderViewModel) {

    val text = viewModel.text.value
    val state = viewModel.state.value
    val start = viewModel.highlightStart.value
    val end = viewModel.highlightEnd.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)    ) {

        // ⭐⭐⭐ 항상 보이는 영역 ⭐⭐⭐
        Text("파일 선택 영역")

        FilePicker { uri ->
            viewModel.openFile(uri)
        }

        Spacer(Modifier.height(20.dp))

        // ⭐ 텍스트
        val annotated = buildAnnotatedString {
            append(text)

            if (start >= 0 && end > start && end <= text.length) {
                addStyle(
                    SpanStyle(background = Color.Yellow),
                    start,
                    end
                )
            }
        }

        Text(
            text = annotated,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "상태: " + when (state) {
                TtsState.Idle -> "대기"
                is TtsState.Speaking -> "읽는 중"
                TtsState.Paused -> "일시정지"
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.speak() }) { Text("재생") }
            Button(onClick = { viewModel.pause() }) { Text("일시정지") }
            Button(onClick = { viewModel.stop() }) { Text("정지") }
        }
    }
}
