package com.example.scriptaudio.ui.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scriptaudio.engine.tts.SentenceHighlighter
import com.example.scriptaudio.viewmodel.MainViewModel

/**
 * ReaderScreen
 *
 * - 전체 문서를 문장 단위로 읽기
 * - 문장 하이라이트 + TTS 지원
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(viewModel: MainViewModel) {
    val text by viewModel.originalText.collectAsState()
    val listState = rememberLazyListState()
    val sentences = remember(text) { SentenceHighlighter().splitSentences(text) }
    var currentSentence by remember { mutableStateOf(0) }

    // 현재 문장 자동 스크롤
    LaunchedEffect(currentSentence) {
        if (currentSentence in sentences.indices) listState.animateScrollToItem(currentSentence)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reader") },
                actions = { IconButton(onClick = { viewModel.speak() }) { Text("▶") } }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            itemsIndexed(sentences) { index, sentence ->
                Text(
                    text = sentence,
                    color = if (index == currentSentence) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}