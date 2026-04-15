package com.example.stockquoteapp.ui.component

/**
 * DetailDialog
 *
 * 기능:
 * - 종목 상세 정보
 * - 확대 차트 표시 (라인 + 영역)
 */

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.stockquoteapp.*

@Composable
fun DetailDialog(
    quote: StockQuote?,
    isLoading: Boolean,
    onClose: () -> Unit,
    language: Language
) {

    Dialog(onDismissRequest = onClose) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                /**
                 * 상단 타이틀
                 */
                Text(
                    text =
                        if (language == Language.KOR)
                            quote?.shortNameKr ?: "-"
                        else
                            quote?.shortName ?: "-",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(12.dp))

                /**
                 * 로딩 상태
                 */
                if (isLoading) {
                    CircularProgressIndicator()
                    return@Column
                }

                /**
                 * 데이터 없음
                 */
                if (quote == null) {
                    Text("No data")
                    return@Column
                }

                /**
                 * 가격 정보
                 */
                Text("Price: ${quote.price}")
                Text("High: ${quote.dayHigh}")
                Text("Low: ${quote.dayLow}")

                Spacer(Modifier.height(16.dp))

                /**
                 * 🔥 차트 (확대)
                 */
                ChartView(quote)

                Spacer(Modifier.weight(1f))

                Button(onClick = onClose) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun ChartView(quote: StockQuote) {

    val points = quote.chartPoints

    if (points.isEmpty()) {
        Text("No chart data")
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {

        val max = points.maxOf { it.close }
        val min = points.minOf { it.close }
        val range = (max - min).takeIf { it != 0.0 } ?: 1.0

        val stepX = size.width / (points.size - 1)

        val path = Path()

        points.forEachIndexed { i, p ->

            val x = i * stepX
            val y = size.height -
                    ((p.close - min) / range * size.height).toFloat()

            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        drawPath(path, Color.Green, style = Stroke(4f))
    }
}