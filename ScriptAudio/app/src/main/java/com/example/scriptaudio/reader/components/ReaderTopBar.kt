package com.example.scriptaudio.ui.reader.components

/**
 * ReaderTopBar
 *
 * 리더 화면 상단 UI
 *
 * 기능
 * - 뒤로가기
 * - 검색
 * - 북마크
 */

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(

    title: String,

    onBack: () -> Unit,

    onSearch: () -> Unit,

    onBookmark: () -> Unit

) {

    TopAppBar(

        title = {

            Text(title)

        },

        navigationIcon = {

            IconButton(

                onClick = onBack

            ) {

                Icon(

                    Icons.Default.ArrowBack,

                    contentDescription = "Back"

                )

            }

        },

        actions = {

            /**
             * 검색 버튼
             */

            IconButton(

                onClick = onSearch

            ) {

                Icon(

                    Icons.Default.Search,

                    contentDescription = "Search"

                )

            }

            /**
             * 북마크 버튼
             */

            IconButton(

                onClick = onBookmark

            ) {

                Icon(

                    Icons.Default.Bookmark,

                    contentDescription = "Bookmark"

                )

            }

        }

    )

}