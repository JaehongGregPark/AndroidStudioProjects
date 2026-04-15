package com.example.stockquoteapp.data

/**
 * StockQuoteRepository (안정 버전)
 *
 * 특징:
 * - REST API 전용 (WebSocket 완전 분리)
 * - OkHttp 안전 사용
 * - GET만 사용 (POST 제거)
 * - response.body 1회만 읽기
 * - IO 스레드에서만 호출
 */

import com.example.stockquoteapp.ChartPoint
import com.example.stockquoteapp.StockQuote
import com.example.stockquoteapp.StockReference
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder

class StockQuoteRepository {

    /**
     * OkHttpClient 단일 인스턴스 (중요)
     */
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * 🔥 종목 리스트 조회
     */
    suspend fun fetchQuotes(
        symbols: List<StockReference>
    ): Result<List<StockQuote>> {

        return try {

            val quotes = symbols.mapNotNull { ref ->
                try {
                    fetchSingleQuote(ref)
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(quotes)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🔥 단일 종목 조회
     */
    private fun fetchSingleQuote(
        reference: StockReference
    ): StockQuote {

        val encoded =
            URLEncoder.encode(reference.symbol, "UTF-8")

        val url =
            "https://query1.finance.yahoo.com/v8/finance/chart/$encoded?interval=1d&range=1d"

        val request = Request.Builder()
            .url(url)
            .get()   // 🔥 반드시 GET
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }

            /**
             * 🔥 body는 반드시 1회만 읽기
             */
            val body = response.body?.string().orEmpty()

            return parseQuote(body, reference)
        }
    }

    /**
     * 🔥 상세 정보 조회 (차트 포함)
     */
    suspend fun fetchQuoteDetail(
        reference: StockReference
    ): Result<StockQuote> {

        return try {

            val quote =
                fetchChartQuote(reference, "5d", true)

            Result.success(quote)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🔥 차트 기반 조회
     */
    private fun fetchChartQuote(
        reference: StockReference,
        range: String,
        includeChart: Boolean
    ): StockQuote {

        val encoded =
            URLEncoder.encode(reference.symbol, "UTF-8")

        val url =
            "https://query1.finance.yahoo.com/v8/finance/chart/$encoded?interval=1d&range=$range"

        val request = Request.Builder()
            .url(url)
            .get()   // 🔥 POST 절대 금지
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }

            val body = response.body?.string().orEmpty()

            return parseChartQuote(body, reference, includeChart)
        }
    }

    /**
     * 🔥 기본 파싱
     */
    private fun parseQuote(
        json: String,
        ref: StockReference
    ): StockQuote {

        /**
         * 실제 구현에서는 JSON 파서 사용 권장
         * (Gson / kotlinx.serialization)
         */

        return StockQuote(
            symbol = ref.symbol,
            shortName = ref.displayName,
            price = extractDouble(json, "regularMarketPrice"),
            previousClose = extractDouble(json, "previousClose")
        )
    }

    /**
     * 🔥 차트 파싱
     */
    private fun parseChartQuote(
        json: String,
        ref: StockReference,
        includeChart: Boolean
    ): StockQuote {

        val price = extractDouble(json, "regularMarketPrice")

        val chart =
            if (includeChart)
                extractChart(json)
            else emptyList()

        return StockQuote(
            symbol = ref.symbol,
            shortName = ref.displayName,
            price = price,
            chartPoints = chart
        )
    }

    /**
     * 🔥 간단 숫자 추출
     */
    private fun extractDouble(
        json: String,
        key: String
    ): Double {

        return Regex("\"$key\":(\\d+\\.?\\d*)")
            .find(json)
            ?.groupValues?.get(1)
            ?.toDoubleOrNull()
            ?: 0.0
    }

    /**
     * 🔥 차트 데이터 생성 (샘플)
     */
    private fun extractChart(
        json: String
    ): List<ChartPoint> {

        // 실제 JSON 파싱 권장
        val now = System.currentTimeMillis()

        return listOf(
            ChartPoint(now - 3000, 100.0),
            ChartPoint(now - 2000, 105.0),
            ChartPoint(now - 1000, 102.0),
            ChartPoint(now, 110.0)
        )
    }
}