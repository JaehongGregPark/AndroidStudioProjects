package com.example.scriptaudio.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * LibraryTab
 *
 * - MainScreen 내 Library 탭 UI
 * - 파일 가져오기, 열기, 삭제 가능
 *
 * @param fileList 파일 목록
 * @param onFileOpen 파일 열기 콜백
 * @param onDelete 파일 삭제 콜백
 * @param onImport 파일 가져오기 콜백
 */
@Composable
fun LibraryTab(
    fileList: List<File>,
    onFileOpen: (File) -> Unit,
    onDelete: (File) -> Unit,
    onImport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 파일 가져오기 버튼
        Button(
            onClick = onImport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("파일 가져오기")
        }

        Spacer(Modifier.height(16.dp))

        // 파일 리스트
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(fileList, key = { it.absolutePath }) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFileOpen(file) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // 파일 이름
                    Text(file.name, modifier = Modifier.weight(1f))

                    // 삭제 버튼
                    Button(onClick = { onDelete(file) }) {
                        Text("삭제")
                    }
                }
            }
        }
    }
}