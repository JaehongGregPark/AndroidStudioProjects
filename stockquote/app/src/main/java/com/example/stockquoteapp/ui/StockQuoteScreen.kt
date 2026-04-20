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

import android.text.Layout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssetCategory.values().forEach {
                    FilterChip(
                        selected = uiState.selectedAsset == it,
                        onClick = { viewModel.selectAsset(it) },
                        label = { Text(it.title(uiState.language)) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            /**
             * [2] 마켓 탭
             */
            val markets =
                AssetMarketMap.map[uiState.selectedAsset].orEmpty()

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

            Spacer(Modifier.height(8.dp))

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
                    modifier = Modifier.fillMaxWidth(), // ✅ 전체 폭
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
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
                            language = uiState.language,
                            modifier = Modifier.fillMaxWidth() // ✅ 핵심
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

@Composable
fun QuoteItem(
    quote: StockQuote,
    onClick: () -> Unit,
    language: Language,
    modifier: Modifier = Modifier
) {
    var prevPrice by remember { mutableStateOf(quote.price) }
    var flash by remember { mutableStateOf(false) }

    // 가격 변경 감지 → 깜빡임
    LaunchedEffect(quote.price) {
        if (prevPrice != null && quote.price != null && prevPrice != quote.price) {
            flash = true
            kotlinx.coroutines.delay(300)
            flash = false
        }
        prevPrice = quote.price
    }

    val flashColor =
        when {
            quote.price != null && prevPrice != null && quote.price > prevPrice!! ->
                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)   // 상승
            quote.price != null && prevPrice != null && quote.price < prevPrice!! ->
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) // 하락
            else -> MaterialTheme.colorScheme.surface
        }

    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (flash) flashColor else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {

            /**
             * [상단] 종목명 + 거래소/통화
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = when (language) {
                            Language.KOR ->
                                quote.shortNameKr ?: quote.shortName ?: quote.symbol
                            else ->
                                quote.shortName ?: quote.symbol
                        }
                    )

                    Text(
                        text = buildString {
                            append(quote.symbol)
                            if (!quote.exchangeName.isNullOrEmpty()) {
                                append(" • ${quote.exchangeName}")
                            }
                            if (!quote.currency.isNullOrEmpty()) {
                                append(" (${quote.currency})")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            /**
             * [하단] 3열 구조 (가격 / 등락 / 퍼센트)
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val change = quote.changeAmount ?: 0.0
                val percent = quote.changePercent ?: 0.0

                val color =
                    if (change >= 0)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary

                // 가격
                Text(
                    text = formatPrice(quote.price),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )

                // 등락
                Text(
                    text = formatSigned(change),
                    modifier = Modifier.weight(1f),
                    color = color,
                    textAlign = TextAlign.End
                )

                // 퍼센트
                Text(
                    text = formatPercent(percent),
                    modifier = Modifier.weight(1f),
                    color = color,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

fun formatPrice(price: Double?): String {
    return price?.let { "%,.0f".format(it) } ?: "-"
}

fun formatSigned(value: Double?): String {
    if (value == null) return "-"
    val sign = if (value >= 0) "+" else ""
    return "$sign${"%,.2f".format(value)}"
}

fun formatPercent(value: Double?): String {
    if (value == null) return "-"
    val sign = if (value >= 0) "+" else ""
    return "$sign${"%.2f".format(value)}%"
}