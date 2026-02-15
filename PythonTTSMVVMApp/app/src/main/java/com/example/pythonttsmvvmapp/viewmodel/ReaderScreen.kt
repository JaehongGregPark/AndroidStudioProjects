package com.example.pythonttsmvvmapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.example.pythonttsmvvmapp.tts.TtsState
import com.example.pythonttsmvvmapp.util.FileReader
import com.example.pythonttsmvvmapp.viewmodel.ReaderViewModel

/**
 * 사용자에게 보여지는 메인 화면
 */
@Composable
fun ReaderScreen(viewModel: ReaderViewModel) {

    val context = LocalContext.current

    /** 상태 */
    val state = viewModel.state.value
    val text = viewModel.text.value
    val start = viewModel.highlightStart.value
    val end = viewModel.highlightEnd.value

    /**
     * 파일 선택 런처
     */
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val content = FileReader.readText(context, uri)
        viewModel.setText(content)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Python TTS Reader",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(16.dp))

        /** 파일 열기 */
        Button(onClick = {
            picker.launch(arrayOf("text/plain", "application/pdf"))
        }) {
            Text("파일 열기")
        }

        Spacer(Modifier.height(16.dp))

        /**
         * 텍스트 + 하이라이트
         */
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(Modifier.height(16.dp))

        /** 상태 표시 */
        Text(
            text = when (state) {
                TtsState.Idle -> "상태: 대기"
                is TtsState.Speaking -> "상태: 읽는 중"
                TtsState.Paused -> "상태: 일시정지"
            }
        )

        Spacer(Modifier.height(16.dp))

        /** 버튼 */
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            when (state) {
                TtsState.Idle -> {
                    Button(
                        onClick = { viewModel.speak() },
                        enabled = text.isNotBlank()
                    ) { Text("재생") }
                }

                is TtsState.Speaking -> {
                    Button(onClick = { viewModel.pause() }) {
                        Text("일시정지")
                    }
                }

                TtsState.Paused -> {
                    Button(onClick = { viewModel.speak() }) {
                        Text("다시재생")
                    }
                }
            }

            Button(onClick = { viewModel.stop() }) {
                Text("정지")
            }
        }
    }
}
