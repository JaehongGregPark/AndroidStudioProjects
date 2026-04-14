package com.example.stockquoteapp

/**
 * 자산 카테고리
 * 상단 탭용
 */
enum class AssetCategory(
    val titleEn: String,
    val titleKr: String
) {

    STOCK(
        titleEn = "Stocks",
        titleKr = "주식"
    ),

    CRYPTO(
        titleEn = "Crypto",
        titleKr = "가상화폐"
    ),

    COMMODITY(
        titleEn = "Commodities",
        titleKr = "원자재"
    )
}

fun AssetCategory.title(language: Language): String {
    return if (language == Language.KOR)
        titleKr
    else
        titleEn
}