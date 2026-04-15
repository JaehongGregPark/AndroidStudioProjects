package com.example.stockquoteapp

/**
 * UI 전체 상태
 *
 * 기능:
 * - 마켓별 데이터 보관
 * - 페이지네이션 상태 관리
 * - 선택 종목 관리
 * - 로딩 상태 관리
 */
data class StockUiState(

    val selectedAsset: AssetCategory = AssetCategory.STOCK,

    /** 현재 선택된 마켓 */
    val selectedMarket: MarketCategory = MarketCategory.NASDAQ,

    /** 마켓별 종목 리스트 */
    val quotesByMarket: Map<MarketCategory, List<StockQuote>> = emptyMap(),

    /** 마켓별 선택된 종목 */
    val selectedSymbolByMarket: Map<MarketCategory, String> = emptyMap(),

    /** 종목 상세 정보 */
    val detailsBySymbol: Map<String, StockQuote> = emptyMap(),

    /**
     * 마켓별 현재 페이지
     * 예:
     * NASDAQ -> 0
     * NYSE -> 2
     */
    val currentPageByMarket: Map<MarketCategory, Int> = emptyMap(),
    val currentPage: Int = 1,

    val totalPages: Int = 1,
    /** 페이지당 표시 개수 (5개) */
    val pageSize: Int = 5,

    val language: Language = Language.ENG,

    /** 리스트 로딩 상태 */
    val isLoading: Boolean = false,

    /** 상세 로딩 상태 */
    val isDetailLoading: Boolean = false,

    /** 상세 다이얼로그 표시 */
    val isDetailVisible: Boolean = false,

    /** 에러 메시지 */
    val errorMessage: String? = null
)

/**
 * 현재 페이지
 */
val StockUiState.currentPage: Int
    get() = currentPageByMarket[selectedMarket] ?: 0


/**
 * 현재 페이지에 표시될 종목 5개
 */
val StockUiState.selectedQuotes: List<StockQuote>
    get() {
        val list = quotesByMarket[selectedMarket].orEmpty()

        val start = currentPage * pageSize
        val end = (start + pageSize).coerceAtMost(list.size)

        if (start >= list.size) return emptyList()

        return list.subList(start, end)
    }


/**
 * 전체 페이지 수
 */
val StockUiState.totalPages: Int
    get() {
        val total = quotesByMarket[selectedMarket].orEmpty().size
        if (total == 0) return 1
        return (total + pageSize - 1) / pageSize
    }


/**
 * 현재 선택된 종목 symbol
 */
val StockUiState.selectedSymbol: String?
    get() = selectedSymbolByMarket[selectedMarket]



/**
 * 현재 선택된 종목 객체
 */
val StockUiState.selectedQuote: StockQuote?
    get() {
        val symbol = selectedSymbol ?: return null
        return detailsBySymbol[symbol]
    }