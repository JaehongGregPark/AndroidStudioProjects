package com.example.scriptaudio.ui.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material3.Button
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * TranslationTab
 *
 * 번역 결과 표시
 */
@Composable
fun TranslationTab(

    text: String,

    onTranslate: () -> Unit

) {

    Column(

        modifier = Modifier.fillMaxSize()

    ) {

        LazyColumn(

            modifier = Modifier
                .weight(1f)
                .padding(16.dp)

        ) {

            item {

                Text(text)

            }

        }

        Button(

            onClick = onTranslate,

            modifier = Modifier.padding(16.dp)

        ) {

            Text("번역 실행")

        }

    }

}