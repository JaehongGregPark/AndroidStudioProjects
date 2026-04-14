package com.example.stockquoteapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * 페이지네이션 UI
 *
 * 예:
 * < 1 2 3 4 5 ... 10 >
 */
@Composable
fun Pagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {

        /**
         * 이전 페이지 버튼
         */
        TextButton(
            onClick = {
                onPageChange(
                    (currentPage - 1).coerceAtLeast(0)
                )
            },
            enabled = currentPage > 0
        ) {
            Text("<")
        }

        /**
         * 페이지 그룹 계산
         * 0~4
         * 5~9
         */
        val start = (currentPage / 5) * 5
        val end = (start + 5).coerceAtMost(totalPages)

        /**
         * 페이지 번호 표시
         */
        for (i in start until end) {

            TextButton(
                onClick = { onPageChange(i) }
            ) {
                Text(
                    text = "${i + 1}",

                    // 현재 페이지 강조
                    fontWeight =
                        if (i == currentPage)
                            FontWeight.Bold
                        else
                            FontWeight.Normal
                )
            }
        }

        /**
         * 다음 페이지 버튼
         */
        TextButton(
            onClick = {
                onPageChange(
                    (currentPage + 1)
                        .coerceAtMost(totalPages - 1)
                )
            },
            enabled = currentPage < totalPages - 1
        ) {
            Text(">")
        }
    }
}