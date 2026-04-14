package com.example.stockquoteapp

object AssetMarketMap {

    val map = mapOf(

        AssetCategory.STOCK to listOf(
            MarketCategory.KOSPI,
            MarketCategory.KOSDAQ,
            MarketCategory.NASDAQ,
            MarketCategory.DOW
        ),

        AssetCategory.CRYPTO to listOf(
            MarketCategory.CRYPTO_TOP,
            MarketCategory.CRYPTO_DEFI
        ),

        AssetCategory.COMMODITY to listOf(
            MarketCategory.OIL,
            MarketCategory.METAL,
            MarketCategory.AGRI
        )
    )
}