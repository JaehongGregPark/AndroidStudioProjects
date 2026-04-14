package com.example.stockquoteapp

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Yahoo Finance 기반 시세 Repository
 *
 * 지원:
 * - 주식
 * - 가상화폐
 * - 원자재
 *
 * 모두 chart API 사용
 */
class StockQuoteRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {

    /**
     * 리스트 시세 로드
     */
    fun fetchQuotes(symbols: List<StockReference>): Result<List<StockQuote>> {
        return runCatching {

            val normalized = symbols
                .map { it.copy(symbol = it.symbol.trim().uppercase()) }
                .filter { it.symbol.isNotBlank() }

            require(normalized.isNotEmpty())

            val quotes = normalized.mapNotNull { reference ->
                runCatching {
                    fetchQuoteFromChart(
                        reference,
                        range = "5d",
                        includeChart = false
                    )
                }.getOrNull()
            }

            if (quotes.isEmpty())
                throw IllegalStateException("No quotes loaded")

            quotes
        }
    }

    /**
     * 상세 시세 로드
     */
    fun fetchQuoteDetail(
        reference: StockReference
    ): Result<StockQuote> {
        return runCatching {
            fetchQuoteFromChart(
                reference,
                range = "1mo",
                includeChart = true
            )
        }
    }

    /**
     * Yahoo chart API 호출
     */
    private fun fetchQuoteFromChart(
        reference: StockReference,
        range: String,
        includeChart: Boolean
    ): StockQuote {

        val encoded =
            URLEncoder.encode(
                reference.symbol,
                Charsets.UTF_8.name()
            )

        val url =
            "https://query1.finance.yahoo.com/v8/finance/chart/$encoded" +
                    "?interval=1d&range=$range"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }

            val body =
                response.body?.string().orEmpty()

            return parseChartQuote(
                body,
                reference,
                includeChart
            )
        }
    }

    /**
     * chart JSON 파싱
     */
    private fun parseChartQuote(
        json: String,
        reference: StockReference,
        includeChart: Boolean
    ): StockQuote {

        val root = JSONObject(json)
        val chart = root.getJSONObject("chart")

        val result =
            chart.getJSONArray("result")
                .getJSONObject(0)

        val meta = result.getJSONObject("meta")

        val quote =
            result.getJSONObject("indicators")
                .getJSONArray("quote")
                .getJSONObject(0)

        val closes = quote.optJSONArray("close")
        val highs = quote.optJSONArray("high")
        val lows = quote.optJSONArray("low")
        val opens = quote.optJSONArray("open")
        val timestamps = result.optJSONArray("timestamp")

        val price =
            meta.optDouble("regularMarketPrice")
                .takeUnless { it.isNaN() }
                ?: closes?.lastNonNullDouble()
                ?: throw IllegalStateException()

        val previous =
            meta.optDouble("previousClose")
                .takeUnless { it.isNaN() }
                ?: closes?.previousNonNullDouble()

        return StockQuote(
            symbol = reference.symbol,
            shortName = reference.displayName,
            shortNameKr = reference.displayNameKr,
            currency = meta.optString("currency"),
            exchangeName = meta.optString("exchangeName"),
            price = price,
            previousClose = previous,
            marketTime = meta.optLong("regularMarketTime"),
            marketCap = null,
            openPrice = opens?.lastNonNullDouble(),
            dayHigh = highs?.lastNonNullDouble(),
            dayLow = lows?.lastNonNullDouble(),
            chartPoints =
                if (includeChart)
                    timestamps.toChartPoints(closes)
                else emptyList()
        )
    }

    private fun JSONArray?.toChartPoints(
        closes: JSONArray?
    ): List<ChartPoint> {

        if (this == null || closes == null)
            return emptyList()

        val list = mutableListOf<ChartPoint>()

        val count = minOf(length(), closes.length())

        for (i in 0 until count) {

            if (isNull(i) || closes.isNull(i))
                continue

            list += ChartPoint(
                timestamp = optLong(i),
                close = closes.optDouble(i)
            )
        }

        return list
    }

    private fun JSONArray.lastNonNullDouble(): Double? {
        for (i in length() - 1 downTo 0) {
            if (!isNull(i)) return optDouble(i)
        }
        return null
    }

    private fun JSONArray.previousNonNullDouble(): Double? {

        var found = false

        for (i in length() - 1 downTo 0) {

            if (!isNull(i)) {

                val v = optDouble(i)

                if (!found) {
                    found = true
                } else {
                    return v
                }
            }
        }
        return null
    }
}