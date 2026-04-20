package com.example.stockquoteapp

// Compose 상태 관리
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockquoteapp.data.WebSocketManager
import com.example.stockquoteapp.data.StockQuoteRepository
// Coroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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


    private val wsManager = WebSocketManager()

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

        wsManager.onMessageReceived = { message ->
            handleRealtimeData(message)
        }

        //wsManager.connect()

        startAutoRefresh()
    }

    private fun startAutoRefresh() {

        viewModelScope.launch {

            while (isActive) {

                delay(5000)

                loadMarket(
                    uiState.selectedMarket,
                    forceRefresh = true
                )
            }
        }
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
                uiState.currentPageByMarket + (market to 1)
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

        uiState = uiState.copy(
            selectedSymbolByMarket =
                uiState.selectedSymbolByMarket +
                        (uiState.selectedMarket to symbol),

            isDetailVisible = true
        )

        ensureDetailLoaded(
            uiState.selectedMarket,
            symbol,
            forceRefresh = true
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

                    updatePaging(quotes)

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
                ?.take(10)
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


    fun changePage(page: Int) {

        val safePage = page.coerceAtLeast(1)

        uiState = uiState.copy(
            currentPageByMarket =
                uiState.currentPageByMarket +
                        (uiState.selectedMarket to safePage)
        )
    }


    private fun updatePaging(quotes: List<StockQuote>) {

        val pageSize = 5

        val totalPages =
            (quotes.size + pageSize - 1) / pageSize

        uiState = uiState.copy(
            totalPages = maxOf(totalPages, 1)
        )
    }


    private fun handleRealtimeData(message: String) {

        /**
         * 예: JSON → 파싱
         * {"symbol":"AAPL","price":190.5}
         */

        try {

            val data = parseMessage(message)

            val updatedMap =
                uiState.detailsBySymbol.toMutableMap()

            updatedMap[data.symbol] =
                updatedMap[data.symbol]?.copy(
                    price = data.price
                ) ?: return

            uiState = uiState.copy(
                detailsBySymbol = updatedMap
            )

        } catch (e: Exception) {
            // ignore
        }
    }

    private fun parseMessage(msg: String): RealtimeData {

        val symbol =
            Regex("\"symbol\":\"(.*?)\"")
                .find(msg)
                ?.groupValues?.get(1)
                ?: ""

        val price =
            Regex("\"price\":(\\d+\\.?\\d*)")
                .find(msg)
                ?.groupValues?.get(1)
                ?.toDoubleOrNull()
                ?: 0.0

        return RealtimeData(symbol, price)
    }

    /**
     * WebSocket 실시간 데이터 모델
     */
    data class RealtimeData(
        val symbol: String,
        val price: Double
    )
}


