package com.example.scriptaudio.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * TranslationTab
 *
 * - MainScreen 내 Translation 탭 UI
 * - 원문 텍스트와 번역 텍스트 표시
 * - 번역 실행 버튼 클릭 시 MainViewModel의 translate() 실행
 *
 * @param originalText 원문 텍스트
 * @param translatedText 번역된 텍스트
 * @param isTranslating 번역 진행 여부
 * @param onTranslate 번역 실행 콜백
 */
@Composable
fun TranslationTab(
    originalText: String,
    translatedText: String,
    isTranslating: Boolean,
    isEnglish: Boolean,
    onTranslate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("번역 전", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))

        // 원문 텍스트 표시 (읽기 전용)
        TextField(
            value = originalText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(Modifier.height(10.dp))
        Text("번역 후", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))

        // 번역 텍스트 표시 (읽기 전용)
        TextField(
            value = translatedText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(Modifier.height(10.dp))

        // 번역 버튼
        Button(
            onClick = onTranslate,
            enabled = !isTranslating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isTranslating) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("번역 중...")
                }
            } else {
                Text("번역")
            }
        }
    }
}