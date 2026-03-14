package com.example.scriptaudio.ui.reader.components

/**
 * ReaderBottomControls
 *
 * 리더 화면 하단 컨트롤
 *
 * 기능
 * - TTS Play
 * - Stop
 * - 페이지 이동
 */

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun ReaderBottomControls(

    currentPage: Int,

    totalPages: Int,

    isReading: Boolean,

    onPlay: () -> Unit,

    onStop: () -> Unit,

    onPrevPage: () -> Unit,

    onNextPage: () -> Unit

) {

    Surface(

        tonalElevation = 4.dp

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)

        ) {

            /**
             * 페이지 표시
             */

            Text(

                text = "Page $currentPage / $totalPages"

            )

            Spacer(Modifier.height(8.dp))

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween

            ) {

                /**
                 * 이전 페이지
                 */

                IconButton(

                    onClick = onPrevPage

                ) {

                    Icon(

                        Icons.Default.SkipPrevious,

                        contentDescription = "Prev Page"

                    )

                }

                /**
                 * TTS 실행
                 */

                if (!isReading) {

                    IconButton(

                        onClick = onPlay

                    ) {

                        Icon(

                            Icons.Default.PlayArrow,

                            contentDescription = "Play"

                        )

                    }

                } else {

                    IconButton(

                        onClick = onStop

                    ) {

                        Icon(

                            Icons.Default.Stop,

                            contentDescription = "Stop"

                        )

                    }

                }

                /**
                 * 다음 페이지
                 */

                IconButton(

                    onClick = onNextPage

                ) {

                    Icon(

                        Icons.Default.SkipNext,

                        contentDescription = "Next Page"

                    )

                }

            }

        }

    }

}