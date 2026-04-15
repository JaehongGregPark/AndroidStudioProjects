package com.example.stockquoteapp.ui

/**
 * StockQuoteScreen
 *
 * 기능:
 * - 자산 탭 / 마켓 탭
 * - Pager 기반 스와이프 페이지
 * - LazyColumn 리스트
 * - Pagination 연동
 * - 상세 다이얼로그
 *
 * 핵심:
 * - Pager ↔ Pagination 동기화
 * - LazyColumn으로 성능 최적화
 */

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.stockquoteapp.*
import com.example.stockquoteapp.ui.component.*


@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun StockQuoteScreen(
    viewModel: StockQuoteViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()

    /**
     * Pager 상태
     * - currentPage (1-based) → pager는 0-based
     */
    val pagerState = rememberPagerState(

        initialPage = (uiState.currentPage - 1).coerceAtLeast(0),
        pageCount = { maxOf(uiState.totalPages, 1) }
    )

    Surface {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            /**
             * [1] 자산 탭
             */
            Row {
                AssetCategory.values().forEach {
                    FilterChip(
                        selected = uiState.selectedAsset == it,
                        onClick = { viewModel.selectAsset(it) },
                        label = { Text(it.title(uiState.language)) }
                    )
                }
            }

            /**
             * [2] 마켓 탭
             */
            val markets =
                AssetMarketMap.map[uiState.selectedAsset].orEmpty()

            FlowRow {
                markets.forEach {
                    FilterChip(
                        selected = uiState.selectedMarket == it,
                        onClick = { viewModel.selectMarket(it) },
                        label = {
                            Text(
                                if (uiState.language == Language.KOR)
                                    it.titleKr else it.titleEn
                            )
                        }
                    )
                }
            }

            /**
             * [3] 스와이프 페이지 (핵심)
             */
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->

                /**
                 * Pager → ViewModel 동기화
                 */
                if (uiState.currentPage != page + 1) {
                    viewModel.changePage(page + 1)
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.selectedQuotes,
                        key = { it.symbol }
                    ) { quote ->

                        QuoteItem(
                            quote = quote,
                            onClick = {
                                viewModel.selectQuote(quote.symbol)
                            },
                            language = uiState.language
                        )
                    }
                }
            }

            /**
             * [4] Pagination → Pager 이동
             */
            Pagination(
                currentPage = uiState.currentPage,
                totalPages = maxOf(uiState.totalPages, 1),
                onPageChange = {
                    scope.launch {
                        pagerState.animateScrollToPage(it - 1)
                    }
                }
            )
        }
    }

    /**
     * [5] 상세 다이얼로그
     */
    if (uiState.isDetailVisible) {
        DetailDialog(
            quote = uiState.selectedQuote,
            isLoading = uiState.isDetailLoading,
            onClose = { viewModel.closeDetail() },
            language = uiState.language
        )
        }
    }
