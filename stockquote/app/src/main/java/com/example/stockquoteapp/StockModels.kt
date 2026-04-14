package com.example.stockquoteapp



data class StockReference(
    val symbol: String,
    /** 영어 이름 */
    val displayName: String,

    /** 한글 이름 */
    val displayNameKr: String? = null
)

data class ChartPoint(
    val timestamp: Long,
    val close: Double
)

data class StockQuote(
    val symbol: String,
    val shortName: String?,
    val shortNameKr: String? = null,
    val currency: String?,
    val exchangeName: String?,
    val price: Double,
    val previousClose: Double?,
    val marketTime: Long?,
    val marketCap: Long?,
    val openPrice: Double? = null,
    val dayHigh: Double? = null,
    val dayLow: Double? = null,
    val chartPoints: List<ChartPoint> = emptyList()
) {
    val changeAmount: Double?
        get() = previousClose?.let { price - it }

    val changePercent: Double?
        get() = previousClose
            ?.takeIf { it != 0.0 }
            ?.let { ((price - it) / it) * 100.0 }
}
