package com.example.stockquoteapp

enum class MarketCategory(val title: String, val description: String) {
    KOSPI("KOSPI", "Top 20 large-cap names in Korea's main market"),
    KOSDAQ("KOSDAQ", "Top 20 large-cap names in Korea's growth market"),
    NASDAQ("NASDAQ", "Top 20 mega-cap Nasdaq stocks"),
    DOW("DOW", "Large-cap Dow Jones components")
}

data class StockReference(
    val symbol: String,
    val displayName: String
)

data class ChartPoint(
    val timestamp: Long,
    val close: Double
)

data class StockQuote(
    val symbol: String,
    val shortName: String?,
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

data class StockUiState(
    val selectedMarket: MarketCategory = MarketCategory.KOSPI,
    val isLoading: Boolean = false,
    val isDetailLoading: Boolean = false,
    val isDetailVisible: Boolean = false,
    val quotesByMarket: Map<MarketCategory, List<StockQuote>> = emptyMap(),
    val selectedSymbolByMarket: Map<MarketCategory, String> = emptyMap(),
    val detailsBySymbol: Map<String, StockQuote> = emptyMap(),
    val errorMessage: String? = null
) {
    val selectedQuotes: List<StockQuote>
        get() = quotesByMarket[selectedMarket].orEmpty()

    val selectedSymbol: String?
        get() = selectedSymbolByMarket[selectedMarket] ?: selectedQuotes.firstOrNull()?.symbol

    val selectedQuote: StockQuote?
        get() {
            val symbol = selectedSymbol ?: return null
            return detailsBySymbol[symbol]
                ?: selectedQuotes.firstOrNull { it.symbol == symbol }
        }
}

object StockCatalog {
    val symbolsByMarket: Map<MarketCategory, List<StockReference>> = mapOf(
        MarketCategory.KOSPI to listOf(
            StockReference("005930.KS", "Samsung Electronics"),
            StockReference("000660.KS", "SK Hynix"),
            StockReference("207940.KS", "Samsung Biologics"),
            StockReference("005380.KS", "Hyundai Motor"),
            StockReference("012330.KS", "Hyundai Mobis"),
            StockReference("068270.KS", "Celltrion"),
            StockReference("035420.KS", "NAVER"),
            StockReference("105560.KS", "KB Financial"),
            StockReference("055550.KS", "Shinhan Financial"),
            StockReference("086790.KS", "Hana Financial"),
            StockReference("051910.KS", "LG Chem"),
            StockReference("012450.KS", "Hanwha Aerospace"),
            StockReference("003670.KS", "POSCO Future M"),
            StockReference("066570.KS", "LG Electronics"),
            StockReference("034730.KS", "SK"),
            StockReference("329180.KS", "HD Hyundai Heavy Industries"),
            StockReference("018260.KS", "Samsung SDS"),
            StockReference("017670.KS", "SK Telecom"),
            StockReference("010130.KS", "Korea Zinc"),
            StockReference("030200.KS", "KT"),
            StockReference("259960.KS", "Krafton")
        ),
        MarketCategory.KOSDAQ to listOf(
            StockReference("247540.KQ", "EcoPro BM"),
            StockReference("086520.KQ", "ECOPRO"),
            StockReference("066970.KQ", "L&F"),
            StockReference("196170.KQ", "Alteogen"),
            StockReference("028300.KQ", "HLB"),
            StockReference("214450.KQ", "PharmaResearch"),
            StockReference("293490.KQ", "Kakao Games"),
            StockReference("263750.KQ", "Pearl Abyss"),
            StockReference("095660.KQ", "NEOWIZ"),
            StockReference("214150.KQ", "Classys"),
            StockReference("145020.KQ", "Hugel"),
            StockReference("091970.KQ", "Nexon Games"),
            StockReference("039030.KQ", "EO Technics"),
            StockReference("112040.KQ", "Wemade"),
            StockReference("237690.KQ", "ST Pharm"),
            StockReference("067310.KQ", "Hana Micron"),
            StockReference("240810.KQ", "Wonik IPS"),
            StockReference("357780.KQ", "Solus Advanced Materials"),
            StockReference("348370.KQ", "Enchem"),
            StockReference("058470.KQ", "Leeno Industrial"),
            StockReference("078600.KQ", "Daejoo Electronic Materials")
        ),
        MarketCategory.NASDAQ to listOf(
            StockReference("NVDA", "NVIDIA"),
            StockReference("AAPL", "Apple"),
            StockReference("GOOGL", "Alphabet Class A"),
            StockReference("MSFT", "Microsoft"),
            StockReference("AMZN", "Amazon"),
            StockReference("AVGO", "Broadcom"),
            StockReference("META", "Meta Platforms"),
            StockReference("TSLA", "Tesla"),
            StockReference("COST", "Costco"),
            StockReference("NFLX", "Netflix"),
            StockReference("ASML", "ASML"),
            StockReference("TMUS", "T-Mobile US"),
            StockReference("CSCO", "Cisco"),
            StockReference("AMD", "AMD"),
            StockReference("LIN", "Linde"),
            StockReference("INTU", "Intuit"),
            StockReference("ISRG", "Intuitive Surgical"),
            StockReference("BKNG", "Booking Holdings"),
            StockReference("PEP", "PepsiCo"),
            StockReference("ADBE", "Adobe")
        ),
        MarketCategory.DOW to listOf(
            StockReference("AAPL", "Apple"),
            StockReference("MSFT", "Microsoft"),
            StockReference("AMZN", "Amazon"),
            StockReference("V", "Visa"),
            StockReference("WMT", "Walmart"),
            StockReference("JPM", "JPMorgan Chase"),
            StockReference("UNH", "UnitedHealth"),
            StockReference("HD", "Home Depot"),
            StockReference("PG", "Procter & Gamble"),
            StockReference("CRM", "Salesforce"),
            StockReference("GS", "Goldman Sachs"),
            StockReference("MCD", "McDonald's"),
            StockReference("CAT", "Caterpillar"),
            StockReference("IBM", "IBM"),
            StockReference("AXP", "American Express"),
            StockReference("JNJ", "Johnson & Johnson"),
            StockReference("HON", "Honeywell"),
            StockReference("TRV", "Travelers"),
            StockReference("CVX", "Chevron"),
            StockReference("DIS", "Disney")
        )
    )
}
