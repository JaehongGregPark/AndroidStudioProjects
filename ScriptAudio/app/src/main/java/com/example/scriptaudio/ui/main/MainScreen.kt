package com.example.scriptaudio.ui.main

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.scriptaudio.viewmodel.MainViewModel
import java.io.File

/**
 * MainScreen
 *
 * - 앱 메인 화면
 * - 탭 구성: Reader / Translation / Library
 * - ViewModel 상태를 읽고 UI와 동기화
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onReaderClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val originalText by viewModel.originalText.collectAsState()
    val translatedText by viewModel.translatedText.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val fileList by viewModel.fileList.collectAsState()

    // 현재 선택된 탭 인덱스
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Reader", "Translation", "Library")

    // SAF 파일 선택기
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        uriToFile(context, uri)?.let { viewModel.openFile(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFiles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScriptAudio") },
                actions = { TextButton(onClick = onSettingsClick) { Text("설정") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // -----------------------------
            // 상단 탭
            // -----------------------------
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // -----------------------------
            // 선택된 탭 화면 표시
            // -----------------------------
            when (selectedTab) {
                0 -> ReaderTab(originalText, { viewModel.updateScript(it) }, { viewModel.speak() })
                1 -> TranslationTab(originalText, translatedText, isTranslating) { viewModel.translate() }
                2 -> LibraryTab(fileList, { viewModel.openFile(it) }, { viewModel.deleteFile(it) }) {
                    filePickerLauncher.launch(arrayOf("text/plain", "application/pdf"))
                }
            }
        }
    }
}

/**
 * URI를 임시 File로 변환
 */
fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.filesDir, uri.lastPathSegment ?: "temp.txt")
        tempFile.outputStream().use { input.copyTo(it) }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}