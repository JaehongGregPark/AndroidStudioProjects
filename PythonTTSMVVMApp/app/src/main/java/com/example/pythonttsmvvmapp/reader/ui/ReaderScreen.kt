package com.example.pythonttsmvvmapp.reader.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.example.pythonttsmvvmapp.reader.viewmodel.ReaderViewModel
import com.example.pythonttsmvvmapp.tts.TtsState
import com.example.pythonttsmvvmapp.ui.FilePicker

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
        modifier = Modifier.Companion
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
                modifier = Modifier.Companion.weight(1f)
            )

            // 최근 파일 화면으로 이동
            Text(
                text = "최근파일",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.Companion
                    .clickable { openRecent() }
                    .padding(8.dp)
            )
        }

        Spacer(Modifier.Companion.height(12.dp))

        // ==================================================
        // ⭐ 파일 선택 버튼
        // ==================================================

        FilePicker { uri: Uri ->
            viewModel.openFile(context, uri)
        }

        Spacer(Modifier.Companion.height(20.dp))

        // ==================================================
        // ⭐ 본문 텍스트
        // ==================================================

        if (text.isBlank()) {

            // 파일이 아직 없을 때
            Box(
                modifier = Modifier.Companion
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
                        SpanStyle(background = Color.Companion.Yellow),
                        start,
                        end
                    )
                }
            }

            Text(
                text = annotated,
                modifier = Modifier.Companion
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            )
        }

        Spacer(Modifier.Companion.height(16.dp))

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

        Spacer(Modifier.Companion.height(12.dp))

        // ==================================================
        // ⭐ 재생 컨트롤 버튼
        // ==================================================

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            when (state) {

                TtsState.Idle -> {
                    Button(onClick = { viewModel.speak() }) {
                        Text("재생")
                    }
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

            Button(onClick = { viewModel.stop(context) }) {
                Text("정지")
            }
        }
    }
}