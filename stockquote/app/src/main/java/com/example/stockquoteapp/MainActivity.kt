package com.example.stockquoteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.stockquoteapp.ui.StockQuoteScreen
import com.example.stockquoteapp.ui.theme.StockQuoteAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: StockQuoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockQuoteAppTheme {
                StockQuoteScreen(viewModel = viewModel)
            }
        }
    }
}
