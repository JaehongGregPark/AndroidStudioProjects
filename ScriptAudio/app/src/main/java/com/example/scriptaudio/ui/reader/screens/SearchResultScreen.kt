package com.example.scriptaudio.ui.reader.screens

/**
 * SearchResultScreen
 *
 * Reader 텍스트 검색 결과 화면
 */

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*

import androidx.compose.material3.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(

    results: List<Int>,

    text: String,

    onOpen: (Int) -> Unit,

    onBack: () -> Unit

) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text("Search Results")

                },

                navigationIcon = {

                    IconButton(

                        onClick = onBack

                    ) {

                        Text("←")

                    }

                }

            )

        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)

        ) {

            items(results) { index ->

                SearchResultItem(

                    index,
                    text,
                    onOpen

                )

            }

        }

    }

}

/**
 * 검색 결과 항목
 */

@Composable
fun SearchResultItem(

    index: Int,

    text: String,

    onOpen: (Int) -> Unit

) {

    val preview =

        text.substring(

            index,

            (index + 80)
                .coerceAtMost(text.length)

        )

    Surface(

        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)

    ) {

        Column(

            modifier = Modifier
                .padding(12.dp)

        ) {

            Text(preview)

            Spacer(Modifier.height(6.dp))

            Button(

                onClick = {

                    onOpen(index)

                }

            ) {

                Text("Go")

            }

        }

    }

}