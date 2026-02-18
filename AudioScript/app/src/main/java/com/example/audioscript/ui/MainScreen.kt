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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audioscript.viewmodel.MainViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {

    val context = LocalContext.current
    val text by viewModel.text.collectAsState(initial = "")
    val scrollState = rememberScrollState()
    val fileName by viewModel.fileName.collectAsState(initial = "")
    val translatedText by viewModel.translatedText.collectAsState(initial = "")

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.loadFromUri(context, it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (fileName.isNotEmpty()) {
            Text(
                text = "파일: $fileName",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { viewModel.createSampleTxt(context) }) {
            Text("샘플 TXT 생성")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.createSamplePdf(context) }) {
            Text("샘플 PDF 생성")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.createFiveMinuteSamples(context) }) {
            Text("5분 분량 10개 생성 (TXT+PDF)")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.checkStorySamples(context) }) {
            Text("생성 파일 확인")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            launcher.launch(arrayOf("*/*"))
        }) {
            Text("파일 불러오기")
        }


        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.translateText(context) },
            enabled = text.isNotEmpty()
        ) {
            Text("번역 + TTS")
        }
        Button(onClick = { viewModel.speak(context) }) {
            Text("읽기")
        }
    }
}
