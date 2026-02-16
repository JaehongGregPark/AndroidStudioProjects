package com.example.pythonttsmvvmapp.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pythonttsmvvmapp.reader.viewmodel.ReaderViewModel

/**
 * 최근 파일 목록 화면
 */
@Composable
fun RecentFilesScreen(
    context: Context,
    viewModel: ReaderViewModel,
    back: () -> Unit
) {
    val list = viewModel.recentFiles.value

    LaunchedEffect(Unit) {
        viewModel.loadRecent(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("최근 파일", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        if (list.isEmpty()) {
            Text("최근 기록이 없습니다.")
        }

        list.forEach { (name, uri) ->
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.openFile(context, Uri.parse(uri))
                        back()
                    }
                    .padding(12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = { back() }) {
            Text("뒤로")
        }
    }
}
