package com.example.stockquoteapp

enum class MarketCategory(
    val titleEn: String,
    val titleKr: String
) {

    // STOCK
    KOSPI("KOSPI", "코스피"),
    KOSDAQ("KOSDAQ", "코스닥"),
    NASDAQ("NASDAQ", "나스닥"),
    DOW("DOW", "다우"),

    // CRYPTO
    CRYPTO_TOP("Top", "상위"),
    CRYPTO_DEFI("DeFi", "디파이"),

    // COMMODITY
    OIL("Oil", "유가"),
    METAL("Metal", "금속"),
    AGRI("Agri", "농산물")
}