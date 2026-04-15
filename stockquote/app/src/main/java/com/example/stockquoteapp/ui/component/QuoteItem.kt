package com.example.stockquoteapp.ui.component

/**
 * QuoteItem
 *
 * 기능:
 * - 카드 UI
 * - 상승/하락 색상
 * - 애니메이션 적용
 */

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.stockquoteapp.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp

@Composable
fun QuoteItem(
    quote: StockQuote,
    onClick: () -> Unit,
    language: Language
) {

    val change = (quote.price ?: 0.0) - (quote.previousClose ?: 0.0)

    val targetColor =
        if (change >= 0) Color.Red else Color.Blue

    /**
     * 색상 애니메이션
     */
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(500)
    )

    Card(onClick = onClick) {

        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                if (language == Language.KOR)
                    quote.shortNameKr ?: quote.symbol
                else
                    quote.shortName ?: quote.symbol
            )

            Text(
                text = "${quote.price}",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${change}",
                color = animatedColor
            )
        }
    }
}