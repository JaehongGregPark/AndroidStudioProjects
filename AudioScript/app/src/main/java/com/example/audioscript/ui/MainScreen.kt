package com.example.audioscript.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.audioscript.viewmodel.MainViewModel
import com.example.audioscript.ui.SettingPanel

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val text by viewModel.text.collectAsState(initial = "")
    val scrollState = rememberScrollState()

    var storyTitle by remember { mutableStateOf("") }
    var isKorean by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.load(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 📄 텍스트 영역
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 📂 파일 불러오기
        Button(
            onClick = { launcher.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("파일 불러오기")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🌍 번역
        Button(
            onClick = { viewModel.translate() },
            enabled = text.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("번역")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ⚙ 설정 패널 호출
        SettingPanel(
            title = storyTitle,
            onTitleChange = { storyTitle = it },
            isKorean = isKorean,
            onLanguageChange = { isKorean = it },
            onGenerateClick = {
                viewModel.generateStory(storyTitle, isKorean)
            }
        )
    }
}
