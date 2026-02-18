package com.example.audioscript.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 설정 패널
 *
 * 기능:
 * ✔ TXT 저장
 * ✔ PDF 저장
 * ✔ TTS 속도 / 피치 조절
 * ✔ 음성 출력
 */
@Composable
fun SettingsPanel(
    speechRate: Float,
    pitch: Float,

    onSpeechRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,

    onSpeak: () -> Unit,
    onExportPdf: () -> Unit,
    onExportTxt: () -> Unit
) {

    Column {

        // ===============================
        // TXT 저장
        // ===============================

        Button(
            onClick = onExportTxt,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 TXT 저장")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ===============================
        // PDF 저장
        // ===============================

        Button(
            onClick = onExportPdf,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📄 PDF 저장")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===============================
        // TTS 설정
        // ===============================

        Text("🔊 음성 속도: ${"%.2f".format(speechRate)}")

        Slider(
            value = speechRate,
            onValueChange = onSpeechRateChange,
            valueRange = 0.5f..2.0f
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("🎵 음성 톤: ${"%.2f".format(pitch)}")

        Slider(
            value = pitch,
            onValueChange = onPitchChange,
            valueRange = 0.5f..2.0f
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSpeak,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔊 읽기")
        }

    }

}
