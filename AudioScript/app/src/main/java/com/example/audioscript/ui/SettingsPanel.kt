package com.example.audioscript.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingPanel(
    title: String,
    onTitleChange: (String) -> Unit,
    isKorean: Boolean,
    onLanguageChange: (Boolean) -> Unit,
    onGenerateClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "소설 생성 설정",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("제목 입력") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("한국어")

                Switch(
                    checked = isKorean,
                    onCheckedChange = onLanguageChange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGenerateClick,
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("소설 생성")
            }
        }
    }
}
