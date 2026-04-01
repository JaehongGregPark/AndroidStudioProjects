package com.example.stockquoteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.stockquoteapp.ui.MovieBrowserApp
import com.example.stockquoteapp.ui.theme.StockQuoteAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockQuoteAppTheme {
                MovieBrowserApp()
            }
        }
    }
}
