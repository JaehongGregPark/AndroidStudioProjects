package com.example.stockquoteapp

// Compose 상태 관리
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// Coroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * StockQuoteViewModel
 *
 * 역할:
 * - 시장 선택 관리
 * - 종목 목록 로딩
 * - 종목 상세 로딩
 * - UI 상태 관리
 *
 * 구조:
 * UI → ViewModel → Repository → API
 */
class StockQuoteViewModel(

    /**
     * 데이터 제공 Repository
     * 기본값으로 자동 생성
     */
    private val repository: StockQuoteRepository = StockQuoteRepository()

) : ViewModel() {

    /**
     * Compose UI 상태
     * mutableStateOf → 변경시 UI 자동 리컴포즈
     */
    var uiState by mutableStateOf(StockUiState())
        private set


    /**
     * ViewModel 생성 시
     * 기본 시장 자동 로드
     */
    init {
        loadMarket(uiState.selectedMarket)
    }



    /**
     * 시장 선택
     *
     * 예:
     * US / KOREA / CRYPTO
     */
    fun selectMarket(market: MarketCategory) {

        uiState = uiState.copy(
            selectedMarket = market,
            errorMessage = null,

            // 마켓 변경시 페이지 0으로 초기화
            currentPageByMarket =
                uiState.currentPageByMarket + (market to 0)
        )

        if (uiState.quotesByMarket[market].isNullOrEmpty()) {
            loadMarket(market)
        } else {
            uiState.selectedSymbolByMarket[market]?.let { symbol ->
                ensureDetailLoaded(market, symbol)
            }
        }
    }

    /**
     * 종목 선택
     *
     * 예:
     * AAPL
     * TSLA
     */
    fun selectQuote(symbol: String) {

        val quotes =
            uiState.quotesByMarket[uiState.selectedMarket].orEmpty()

        // 존재하지 않는 종목 방어
        if (quotes.none { it.symbol == symbol }) return

        // 선택 종목 상태 저장
        uiState = uiState.copy(
            selectedSymbolByMarket =
                uiState.selectedSymbolByMarket +
                        (uiState.selectedMarket to symbol),

            // 상세 화면 표시
            isDetailVisible = true
        )

        // 상세 정보 로딩
        ensureDetailLoaded(
            uiState.selectedMarket,
            symbol,
            forceRefresh = false
        )
    }


    /**
     * 상세 화면 닫기
     */
    fun closeDetail() {
        uiState = uiState.copy(
            isDetailVisible = false,
            isDetailLoading = false
        )
    }


    /**
     * 재시도 버튼
     */
    fun retry() {
        loadMarket(
            uiState.selectedMarket,
            forceRefresh = true
        )
    }


    /**
     * 시장 데이터 로딩
     *
     * quotes 리스트 로딩
     */
    private fun loadMarket(
        market: MarketCategory,
        forceRefresh: Boolean = false
    ) {

        val symbols =
            StockCatalog.symbolsByMarket[market].orEmpty()

        // 종목 없으면 바로 종료
        if (symbols.isEmpty()) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No symbols"
            )
            return
        }

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {

            try {

                val result = withContext(Dispatchers.IO) {
                    repository.fetchQuotes(symbols)
                }

                result.onSuccess { quotes ->

                    val selected =
                        quotes.firstOrNull()?.symbol

                    uiState = uiState.copy(
                        quotesByMarket =
                            uiState.quotesByMarket +
                                    (market to quotes),

                        selectedSymbolByMarket =
                            selected?.let {
                                uiState.selectedSymbolByMarket +
                                        (market to it)
                            } ?: uiState.selectedSymbolByMarket,

                        selectedMarket = market,
                        isLoading = false
                    )
                }

                result.onFailure {

                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = it.message
                    )
                }

            } catch (e: Exception) {

                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * 종목 상세 정보 로딩
     */
    private fun ensureDetailLoaded(
        market: MarketCategory,
        symbol: String,
        forceRefresh: Boolean = false
    ) {

        // 이미 detail 있으면 skip
        if (!forceRefresh &&
            uiState.detailsBySymbol.containsKey(symbol)
        ) return

        // 종목 reference 검색
        val reference =
            StockCatalog.symbolsByMarket[market]
                .orEmpty()
                .firstOrNull { it.symbol == symbol }
                ?: StockReference(
                    symbol = symbol,
                    displayName = symbol
                )

        // 상세 로딩 상태
        uiState = uiState.copy(
            isDetailLoading = true
        )

        viewModelScope.launch {

            // API 호출
            val result = withContext(Dispatchers.IO) {
                repository.fetchQuoteDetail(reference)
            }

            result
                .onSuccess { detail ->

                    // detail 저장
                    uiState = uiState.copy(
                        isDetailLoading = false,

                        detailsBySymbol =
                            uiState.detailsBySymbol +
                                    (detail.symbol to detail)
                    )
                }

                .onFailure {

                    // 실패시 로딩만 해제
                    uiState = uiState.copy(
                        isDetailLoading = false
                    )
                }
        }
    }

    /**
     * 페이지 변경
     *
     * 예:
     * 0 -> 1
     * 1 -> 2
     */
    fun changePage(page: Int) {

        uiState = uiState.copy(
            currentPageByMarket =
                uiState.currentPageByMarket +
                        (uiState.selectedMarket to page)
        )
    }

    fun setLanguage(language: Language) {
        uiState = uiState.copy(language = language)
    }

    fun selectAsset(asset: AssetCategory) {

        val firstMarket =
            AssetMarketMap.map[asset]?.first()

        uiState = uiState.copy(
            selectedAsset = asset,
            selectedMarket = firstMarket ?: uiState.selectedMarket
        )

        loadMarket(uiState.selectedMarket, true)
    }
}