package com.example.scriptaudio.ui.reader.components

/**
 * ReaderSearchBar
 *
 * 리더 텍스트 검색 UI
 *
 * 기능
 * - 문장 검색
 * - 검색 결과 표시
 */

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReaderSearchBar(

    onSearch: (String) -> Unit

) {

    var query by remember {

        mutableStateOf("")

    }

    Column(

        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)

    ) {

        TextField(

            value = query,

            onValueChange = {

                query = it

            },

            modifier = Modifier.fillMaxWidth(),

            placeholder = {

                Text("Search text...")

            }

        )

        Spacer(Modifier.height(8.dp))

        Button(

            onClick = {

                onSearch(query)

            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("Search")

        }

    }

}