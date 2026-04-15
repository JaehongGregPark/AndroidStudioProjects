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
    val currency: String?="USD",
    val exchangeName: String?="",
    val price: Double? = null,
    val previousClose: Double?=null,
    val marketTime: Long?=null,
    val marketCap: Long?=null,
    val openPrice: Double? = null,
    val dayHigh: Double? = null,
    val dayLow: Double? = null,
    val chartPoints: List<ChartPoint> = emptyList()
) {
    val changeAmount: Double?
        get() = previousClose?.let { prev ->
            (price ?: 0.0) - prev }

    val changePercent: Double?
        get() = previousClose
            ?.takeIf { it != 0.0 }
            ?.let { prev ->
                ((price ?: 0.0) - prev) / prev * 100.0}
}
