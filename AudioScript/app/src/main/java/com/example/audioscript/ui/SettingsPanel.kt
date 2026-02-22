package com.example.audioscript.ui

/**
 * ===============================
 * SettingsPanel.kt
 * ===============================
 *
 * 역할:
 *
 * ✔ TXT 저장
 * ✔ PDF 저장
 * ✔ 음성 속도 조절
 * ✔ 음성 피치 조절
 * ✔ 음성 출력
 *
 * 소설 생성 기능은 포함하지 않는다.
 * → MainScreen 에서 처리
 */

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SettingsPanel(

    /**
     * TTS 상태값
     */
    speechRate: Float,

    pitch: Float,


    /**
     * 이벤트 콜백
     */

    onSpeechRateChange: (Float) -> Unit,

    onPitchChange: (Float) -> Unit,

    onSpeak: () -> Unit,

    onExportPdf: () -> Unit,

    onExportTxt: () -> Unit

) {

    Column {

        /**
         * TXT 저장
         */

        Button(

            onClick = onExportTxt,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("📄 TXT 저장")

        }


        Spacer(modifier = Modifier.height(8.dp))


        /**
         * PDF 저장
         */

        Button(

            onClick = onExportPdf,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("📄 PDF 저장")

        }


        Spacer(modifier = Modifier.height(16.dp))


        /**
         * 음성 속도
         */

        Text(

            "🔊 음성 속도: ${"%.2f".format(speechRate)}"

        )


        Slider(

            value = speechRate,

            onValueChange = onSpeechRateChange,

            valueRange = 0.5f..2.0f

        )


        Spacer(modifier = Modifier.height(8.dp))


        /**
         * 음성 피치
         */

        Text(

            "🎵 음성 톤: ${"%.2f".format(pitch)}"

        )


        Slider(

            value = pitch,

            onValueChange = onPitchChange,

            valueRange = 0.5f..2.0f

        )


        Spacer(modifier = Modifier.height(12.dp))


        /**
         * 음성 출력
         */

        Button(

            onClick = onSpeak,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("🔊 읽기")

        }

    }

}