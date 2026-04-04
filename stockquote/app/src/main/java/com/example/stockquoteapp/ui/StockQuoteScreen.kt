package com.example.stockquoteapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockquoteapp.ChartPoint
import com.example.stockquoteapp.MarketCategory
import com.example.stockquoteapp.StockQuote
import com.example.stockquoteapp.StockQuoteViewModel
import com.example.stockquoteapp.StockUiState
import com.example.stockquoteapp.ui.theme.StockQuoteAppTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun StockQuoteScreen(
    viewModel: StockQuoteViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    StockQuoteScreen(
        uiState = uiState,
        onMarketSelected = viewModel::selectMarket,
        onQuoteSelected = viewModel::selectQuote,
        onRetry = viewModel::retry,
        onCloseDetail = viewModel::closeDetail
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StockQuoteScreen(
    uiState: StockUiState,
    onMarketSelected: (MarketCategory) -> Unit,
    onQuoteSelected: (String) -> Unit,
    onRetry: () -> Unit,
    onCloseDetail: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Market Leaders",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tap a stock to open a richer detail section with price chart and trading data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MarketCategory.entries.forEach { market ->
                    FilterChip(
                        selected = uiState.selectedMarket == market,
                        onClick = { onMarketSelected(market) },
                        label = { Text(market.title) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusCard(
                            text = uiState.errorMessage,
                            background = MaterialTheme.colorScheme.errorContainer,
                            textColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    MarketContent(
                        uiState = uiState,
                        onQuoteSelected = onQuoteSelected
                    )
                }
            }
        }
    }

    if (uiState.isDetailVisible) {
        uiState.selectedQuote?.let { quote ->
            DetailDialog(
                quote = quote,
                isLoading = uiState.isDetailLoading,
                onClose = onCloseDetail
            )
        }
    }
}

@Composable
private fun ColumnScope.MarketContent(
    uiState: StockUiState,
    onQuoteSelected: (String) -> Unit
) {
    val quotes = uiState.selectedQuotes
    val selectedQuote = uiState.selectedQuote

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = uiState.selectedMarket.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        quotes.forEach { quote ->
            QuoteListItem(
                quote = quote,
                selected = selectedQuote?.symbol == quote.symbol,
                onClick = { onQuoteSelected(quote.symbol) }
            )
        }
    }
}

@Composable
private fun QuoteListItem(
    quote: StockQuote,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isPositive = (quote.changeAmount ?: 0.0) >= 0.0
    val accent = if (isPositive) Color(0xFF0C7A43) else Color(0xFFB3261E)
    val percentText = quote.changePercent?.let { String.format(Locale.US, "%.2f%%", it) } ?: "-"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.White,
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quote.shortName ?: quote.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = quote.symbol,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatNumber(quote.price, quote.currency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quote.exchangeName ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${if (isPositive) "+" else ""}$percentText",
                style = MaterialTheme.typography.bodyMedium,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun QuoteDetailSection(
    quote: StockQuote,
    isLoading: Boolean
) {
    val change = quote.changeAmount
    val isPositive = (change ?: 0.0) >= 0.0
    val accent = if (isPositive) Color(0xFF0C7A43) else Color(0xFFB3261E)
    val percentText = quote.changePercent?.let { String.format(Locale.US, "%.2f%%", it) } ?: "-"
    val changeText = change?.let { formatNumber(it, quote.currency) } ?: "-"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quote.shortName ?: quote.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = quote.symbol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.width(24.dp).height(24.dp), strokeWidth = 2.dp)
            }
        }

        Text(
            text = formatNumber(quote.price, quote.currency),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${if (isPositive) "+" else ""}$changeText ($percentText)",
            style = MaterialTheme.typography.titleMedium,
            color = accent
        )

        PriceChartCard(points = quote.chartPoints, accent = accent)

        InfoGrid(
            pairs = listOf(
                "Exchange" to (quote.exchangeName ?: "-"),
                "Currency" to (quote.currency ?: "-"),
                "Open" to quote.openPrice?.let { formatNumber(it, quote.currency) }.orDash(),
                "High" to quote.dayHigh?.let { formatNumber(it, quote.currency) }.orDash(),
                "Low" to quote.dayLow?.let { formatNumber(it, quote.currency) }.orDash(),
                "Updated" to quote.marketTime?.let { formatDateTime(it) }.orDash()
            )
        )
    }
}

@Composable
private fun DetailDialog(
    quote: StockQuote,
    isLoading: Boolean,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock Detail",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onClose) {
                        Text("Close")
                    }
                }

                QuoteDetailSection(
                    quote = quote,
                    isLoading = isLoading
                )

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to List")
                }
            }
        }
    }
}

@Composable
private fun PriceChartCard(points: List<ChartPoint>, accent: Color) {
    val axisColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "1M Price Chart",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (points.size < 2) {
            Text(
                text = "Chart data is not available for this symbol.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val minPrice = points.minOf { it.close }
            val maxPrice = points.maxOf { it.close }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Low ${formatCompactNumber(minPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "High ${formatCompactNumber(maxPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val path = Path()
                val minY = points.minOf { it.close }.toFloat()
                val maxY = points.maxOf { it.close }.toFloat()
                val yRange = max(1f, maxY - minY)
                val xStep = size.width / max(1, points.lastIndex)

                points.forEachIndexed { index, point ->
                    val x = index * xStep
                    val normalized = ((point.close.toFloat() - minY) / yRange)
                    val y = size.height - (normalized * size.height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawLine(
                    color = axisColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
                drawPath(
                    path = path,
                    color = accent,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoGrid(pairs: List<Pair<String, String>>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        pairs.forEach { (label, value) ->
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatusCard(text: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = text, color = textColor, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun formatNumber(value: Double, currency: String?): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = if (value < 10) 2 else 0
    }
    val amount = formatter.format(value)
    return if (currency.isNullOrBlank()) amount else "$amount $currency"
}

private fun formatCompactNumber(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2
    }
    return formatter.format(value)
}

private fun formatDateTime(epochSeconds: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(epochSeconds * 1000L))
}

private fun String?.orDash(): String = this ?: "-"

@Preview(showBackground = true)
@Composable
private fun StockQuoteScreenPreview() {
    StockQuoteAppTheme {
        StockQuoteScreen(
            uiState = StockUiState(
                selectedMarket = MarketCategory.NASDAQ,
                quotesByMarket = mapOf(
                    MarketCategory.NASDAQ to listOf(
                        StockQuote(
                            symbol = "AAPL",
                            shortName = "Apple Inc.",
                            currency = "USD",
                            exchangeName = "NasdaqGS",
                            price = 198.45,
                            previousClose = 194.50,
                            marketTime = 1_712_000_000,
                            marketCap = null
                        ),
                        StockQuote(
                            symbol = "MSFT",
                            shortName = "Microsoft",
                            currency = "USD",
                            exchangeName = "NasdaqGS",
                            price = 425.77,
                            previousClose = 420.32,
                            marketTime = 1_712_000_000,
                            marketCap = null
                        )
                    )
                ),
                selectedSymbolByMarket = mapOf(MarketCategory.NASDAQ to "AAPL"),
                detailsBySymbol = mapOf(
                    "AAPL" to StockQuote(
                        symbol = "AAPL",
                        shortName = "Apple Inc.",
                        currency = "USD",
                        exchangeName = "NasdaqGS",
                        price = 198.45,
                        previousClose = 194.50,
                        marketTime = 1_712_000_000,
                        marketCap = null,
                        openPrice = 196.20,
                        dayHigh = 199.10,
                        dayLow = 195.80,
                        chartPoints = listOf(
                            ChartPoint(1, 186.0),
                            ChartPoint(2, 188.5),
                            ChartPoint(3, 191.2),
                            ChartPoint(4, 190.0),
                            ChartPoint(5, 194.8),
                            ChartPoint(6, 198.45)
                        )
                    )
                )
            ),
            onMarketSelected = {},
            onQuoteSelected = {},
            onRetry = {},
            onCloseDetail = {}
        )
    }
}
