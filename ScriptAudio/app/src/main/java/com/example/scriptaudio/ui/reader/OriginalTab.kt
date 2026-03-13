package com.example.scriptaudio.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * OriginalTab
 *
 * 원문 텍스트 표시
 */
@Composable
fun OriginalTab(

    text: String

) {

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)

    ) {

        item {

            Text(text)

        }

    }

}