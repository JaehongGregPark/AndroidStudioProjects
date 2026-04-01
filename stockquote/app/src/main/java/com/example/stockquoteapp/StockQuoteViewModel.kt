package com.example.stockquoteapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StockQuoteViewModel(
    private val repository: StockQuoteRepository = StockQuoteRepository()
) : ViewModel() {
    var uiState by mutableStateOf(StockUiState())
        private set

    init {
        loadMarket(uiState.selectedMarket)
    }

    fun selectMarket(market: MarketCategory) {
        uiState = uiState.copy(selectedMarket = market, errorMessage = null)
        if (uiState.quotesByMarket[market].isNullOrEmpty()) {
            loadMarket(market)
        } else {
            uiState.selectedSymbol?.let { symbol ->
                ensureDetailLoaded(market, symbol)
            }
        }
    }

    fun selectQuote(symbol: String) {
        val quotes = uiState.quotesByMarket[uiState.selectedMarket].orEmpty()
        if (quotes.none { it.symbol == symbol }) return

        uiState = uiState.copy(
            selectedSymbolByMarket = uiState.selectedSymbolByMarket + (uiState.selectedMarket to symbol),
            isDetailVisible = true
        )
        ensureDetailLoaded(uiState.selectedMarket, symbol, forceRefresh = true)
    }

    fun closeDetail() {
        uiState = uiState.copy(isDetailVisible = false, isDetailLoading = false)
    }

    fun retry() {
        loadMarket(uiState.selectedMarket, forceRefresh = true)
    }

    private fun loadMarket(
        market: MarketCategory,
        forceRefresh: Boolean = false
    ) {
        if (!forceRefresh && !uiState.quotesByMarket[market].isNullOrEmpty()) return

        val symbols = StockCatalog.symbolsByMarket[market].orEmpty()
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.fetchQuotes(symbols)
            }

            result
                .onSuccess { quotes ->
                    val selectedSymbol = uiState.selectedSymbolByMarket[market]
                        ?: quotes.firstOrNull()?.symbol
                    uiState = uiState.copy(
                        selectedMarket = market,
                        isLoading = false,
                        errorMessage = null,
                        quotesByMarket = uiState.quotesByMarket + (market to quotes),
                        selectedSymbolByMarket = selectedSymbol?.let {
                            uiState.selectedSymbolByMarket + (market to it)
                        } ?: uiState.selectedSymbolByMarket
                    )
                    selectedSymbol?.let { ensureDetailLoaded(market, it) }
                }
                .onFailure { throwable ->
                    uiState = uiState.copy(
                        selectedMarket = market,
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load quotes."
                    )
                }
        }
    }

    private fun ensureDetailLoaded(
        market: MarketCategory,
        symbol: String,
        forceRefresh: Boolean = false
    ) {
        if (!forceRefresh && uiState.detailsBySymbol.containsKey(symbol)) return

        val reference = StockCatalog.symbolsByMarket[market]
            .orEmpty()
            .firstOrNull { it.symbol == symbol }
            ?: StockReference(symbol = symbol, displayName = symbol)

        uiState = uiState.copy(isDetailLoading = true)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.fetchQuoteDetail(reference)
            }

            result
                .onSuccess { detail ->
                    uiState = uiState.copy(
                        isDetailLoading = false,
                        detailsBySymbol = uiState.detailsBySymbol + (detail.symbol to detail)
                    )
                }
                .onFailure {
                    uiState = uiState.copy(isDetailLoading = false)
                }
        }
    }
}
