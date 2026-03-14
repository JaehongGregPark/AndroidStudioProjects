package com.example.scriptaudio.ui.reader

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * HighlightReader
 *
 * 현재 읽는 문장 강조
 */
@Composable
fun HighlightReader(

    sentences: List<String>,

    currentIndex: Int

) {

    LazyColumn {

        itemsIndexed(sentences) { index, sentence ->

            Text(

                text = sentence,

                color =
                    if (index == currentIndex)
                        Color.Red
                    else
                        MaterialTheme.colorScheme.onBackground

            )

        }

    }
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex) {

        listState.animateScrollToItem(
            currentIndex
        )

    }
}