package com.example.stockquoteapp.ui.component

/**
 * Pagination UI Component
 *
 * 기능:
 * - 페이지 네비게이션 UI
 * - "< 1 2 3 ... 10 >" 형태 지원
 * - 현재 페이지 강조 표시
 * - 이전/다음 버튼 지원
 *
 * 특징:
 * - 항상 최대 5개 페이지만 표시 (가독성 유지)
 * - 양쪽 생략(...) 자동 처리
 * - Material3 스타일 기반
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Pagination 메인 컴포넌트
 *
 * @param currentPage 현재 페이지 (1부터 시작)
 * @param totalPages 전체 페이지 수
 * @param onPageChange 페이지 변경 콜백
 */
@Composable
fun Pagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {

    /**
     * 화면에 표시할 페이지 개수
     * - UX상 5개가 가장 적절
     */
    val visibleCount = 5

    /**
     * 시작 페이지 계산
     *
     * 예:
     * current = 8 → start = 6
     */
    val startPage = maxOf(
        1,
        currentPage - visibleCount / 2
    )

    /**
     * 끝 페이지 계산
     */
    val endPage = minOf(
        totalPages,
        startPage + visibleCount - 1
    )

    /**
     * 전체 페이지 UI
     */
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        /**
         * [1] 이전 버튼 "<"
         * - 첫 페이지에서는 비활성화
         */
        TextButton(
            onClick = { onPageChange(currentPage - 1) },
            enabled = currentPage > 1
        ) {
            Text("<")
        }

        /**
         * [2] 첫 페이지 + 생략 (...)
         *
         * startPage > 1이면 표시
         */
        if (startPage > 1) {

            PageButton(
                page = 1,
                currentPage = currentPage,
                onClick = onPageChange
            )

            if (startPage > 2) {
                Text(
                    text = "...",
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        /**
         * [3] 중앙 페이지 영역
         */
        for (page in startPage..endPage) {

            PageButton(
                page = page,
                currentPage = currentPage,
                onClick = onPageChange
            )
        }

        /**
         * [4] 마지막 페이지 + 생략 (...)
         */
        if (endPage < totalPages) {

            if (endPage < totalPages - 1) {
                Text(
                    text = "...",
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            PageButton(
                page = totalPages,
                currentPage = currentPage,
                onClick = onPageChange
            )
        }

        /**
         * [5] 다음 버튼 ">"
         * - 마지막 페이지에서 비활성화
         */
        TextButton(
            onClick = { onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages
        ) {
            Text(">")
        }
    }
}

/**
 * 개별 페이지 버튼
 *
 * 역할:
 * - 현재 페이지 강조
 * - 클릭 시 페이지 이동
 */
@Composable
private fun PageButton(
    page: Int,
    currentPage: Int,
    onClick: (Int) -> Unit
) {

    val isSelected = page == currentPage

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .background(
                color =
                    if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick(page) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = page.toString(),

            /**
             * 선택된 페이지 강조
             */
            color =
                if (isSelected)
                    Color.White
                else
                    MaterialTheme.colorScheme.onSurface,

            fontWeight =
                if (isSelected)
                    FontWeight.Bold
                else
                    FontWeight.Normal
        )
    }
}