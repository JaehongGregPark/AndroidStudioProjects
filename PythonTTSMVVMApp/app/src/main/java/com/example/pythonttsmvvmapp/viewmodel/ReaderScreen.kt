package com.example.pythonttsmvvmapp.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
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

/**
 * 📖 책을 읽는 메인 화면
 *
 * 기능:
 * ✔ 파일 선택
 * ✔ 파일 이름 표시
 * ✔ 텍스트 표시
 * ✔ TTS 하이라이트
 * ✔ 재생 / 일시정지 / 정지
 * ✔ 최근 파일 화면 이동
 */
@Composable
fun ReaderScreen(
    context: Context,
    viewModel: ReaderViewModel,
    openRecent: () -> Unit
) {

    // --------------------------------------------------
    // ViewModel 상태 구독
    // --------------------------------------------------

    val text = viewModel.text.value
    val state = viewModel.state.value
    val start = viewModel.highlightStart.value
    val end = viewModel.highlightEnd.value
    val fileName = viewModel.fileName.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        // ==================================================
        // ⭐ 상단 영역
        // ==================================================

        Row(horizontalArrangement = Arrangement.SpaceBetween) {

            // 현재 열린 파일 이름
            Text(
                text = if (fileName.isBlank()) "파일 없음" else fileName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            // 최근 파일 화면으로 이동
            Text(
                text = "최근파일",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { openRecent() }
                    .padding(8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // ==================================================
        // ⭐ 파일 선택 버튼
        // ==================================================

        FilePicker { uri: Uri ->
            viewModel.openFile(context, uri)
        }

        Spacer(Modifier.height(20.dp))

        // ==================================================
        // ⭐ 본문 텍스트
        // ==================================================

        if (text.isBlank()) {

            // 파일이 아직 없을 때
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text("파일을 선택해주세요.")
            }

        } else {

            // 하이라이트 적용 텍스트
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
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            )
        }

        Spacer(Modifier.height(16.dp))

        // ==================================================
        // ⭐ 상태 표시
        // ==================================================

        Text(
            "상태: " + when (state) {
                TtsState.Idle -> "대기"
                is TtsState.Speaking -> "읽는 중"
                TtsState.Paused -> "일시정지"
            }
        )

        Spacer(Modifier.height(12.dp))

        // ==================================================
        // ⭐ 재생 컨트롤 버튼
        // ==================================================

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            when (state) {

                TtsState.Idle -> {
                    Button(onClick = { viewModel.speak(context) }) {
                        Text("재생")
                    }
                }

                is TtsState.Speaking -> {
                    Button(onClick = { viewModel.pause() }) {
                        Text("일시정지")
                    }
                }

                TtsState.Paused -> {
                    Button(onClick = { viewModel.speak(context) }) {
                        Text("다시재생")
                    }
                }
            }

            Button(onClick = { viewModel.stop(context) }) {
                Text("정지")
            }
        }
    }
}
