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

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {

    val context = LocalContext.current
    val text by viewModel.text.collectAsState()
    val scrollState = rememberScrollState()

    // 📂 파일 선택 런처
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            uri?.let {
                viewModel.loadTextFromFile(
                    context.contentResolver,
                    it
                )
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.createSampleFileAndLoad(context)
            }
        ) {
            Text("샘플 파일 생성")
        }

        Button(
            onClick = {
                viewModel.createSamplePdfAndLoad(context)
            }
        ) {
            Text("샘플 PDF 생성")
        }
        Button(
            onClick = {
                filePickerLauncher.launch(
                    arrayOf("text/plain", "application/pdf")
                )
            }
        ) {
            Text("파일 불러오기")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.speakOriginal() }) {
            Text("원문 읽기")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.speakTranslated() }) {
            Text("번역 읽기")
        }
    }
}
