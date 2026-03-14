package com.example.scriptaudio.ui.reader.screens

/**
 * BookmarkScreen
 *
 * Reader 북마크 목록 화면
 *
 * 기능
 * - 북마크 목록 표시
 * - 북마크 이동
 * - 북마크 삭제
 */

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.scriptaudio.engine.bookmark.Bookmark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(

    bookmarks: List<Bookmark>,

    onOpen: (Bookmark) -> Unit,

    onDelete: (Bookmark) -> Unit,

    onBack: () -> Unit

) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text("Bookmarks")

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

            items(bookmarks) { bookmark ->

                BookmarkItem(

                    bookmark,
                    onOpen,
                    onDelete

                )

            }

        }

    }

}

/**
 * 북마크 항목
 */

@Composable
fun BookmarkItem(

    bookmark: Bookmark,

    onOpen: (Bookmark) -> Unit,

    onDelete: (Bookmark) -> Unit

) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween

    ) {

        Column {

            Text(

                text = "Page ${bookmark.page}"

            )

            Text(

                text = bookmark.filePath

            )

        }

        Row {

            Button(

                onClick = {

                    onOpen(bookmark)

                }

            ) {

                Text("Open")

            }

            Spacer(Modifier.width(8.dp))

            Button(

                onClick = {

                    onDelete(bookmark)

                }

            ) {

                Text("Delete")

            }

        }

    }

}