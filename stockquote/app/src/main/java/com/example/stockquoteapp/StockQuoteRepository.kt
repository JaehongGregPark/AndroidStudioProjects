package com.example.stockquoteapp

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class StockQuoteRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    fun fetchQuotes(symbols: List<StockReference>): Result<List<StockQuote>> {
        return runCatching {
            val normalizedSymbols = symbols
                .map { it.copy(symbol = it.symbol.trim().uppercase()) }
                .filter { it.symbol.isNotBlank() }
            require(normalizedSymbols.isNotEmpty()) { "No stock symbols configured." }

            val quotes = normalizedSymbols.mapNotNull { reference ->
                runCatching { fetchQuoteFromChart(reference, "5d", includeChart = false) }.getOrNull()
            }

            if (quotes.isEmpty()) {
                throw IllegalStateException("No quotes could be loaded for this market.")
            }

            quotes
        }
    }

    fun fetchQuoteDetail(reference: StockReference): Result<StockQuote> {
        return runCatching {
            fetchQuoteFromChart(reference, "1mo", includeChart = true)
        }
    }

    private fun fetchQuoteFromChart(
        reference: StockReference,
        range: String,
        includeChart: Boolean
    ): StockQuote {
        val encodedSymbol = URLEncoder.encode(reference.symbol, Charsets.UTF_8.name())
        val url =
            "https://query1.finance.yahoo.com/v8/finance/chart/$encodedSymbol?interval=1d&range=$range"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to load quotes. HTTP ${response.code}")
            }

            val body = response.body?.string().orEmpty()
            return parseChartQuote(body, reference, includeChart)
        }
    }

    private fun parseChartQuote(
        json: String,
        reference: StockReference,
        includeChart: Boolean
    ): StockQuote {
        val root = JSONObject(json)
        val chart = root.getJSONObject("chart")
        val error = chart.optJSONObject("error")
        if (error != null && !error.equals(JSONObject.NULL)) {
            val message = error.optString("description").ifBlank { "Unknown error" }
            throw IllegalStateException(message)
        }

        val resultArray = chart.optJSONArray("result")
            ?: throw IllegalStateException("No stock data was found.")
        if (resultArray.length() == 0) {
            throw IllegalStateException("No results were returned.")
        }

        val result = resultArray.getJSONObject(0)
        val meta = result.getJSONObject("meta")
        val indicators = result.optJSONObject("indicators")
        val quoteArray = indicators?.optJSONArray("quote")
        val quoteObject = if (quoteArray != null && quoteArray.length() > 0) {
            quoteArray.getJSONObject(0)
        } else {
            null
        }

        val closes = quoteObject?.optJSONArray("close")
        val highs = quoteObject?.optJSONArray("high")
        val lows = quoteObject?.optJSONArray("low")
        val opens = quoteObject?.optJSONArray("open")
        val timestamps = result.optJSONArray("timestamp")

        val price = meta.optDouble("regularMarketPrice").takeUnless { it.isNaN() }
            ?: closes?.lastNonNullDouble()
            ?: throw IllegalStateException("Current price is unavailable.")

        val previousClose = meta.optDouble("previousClose").takeUnless { it.isNaN() }
            ?: closes?.previousNonNullDouble()

        return StockQuote(
            symbol = meta.optString("symbol").ifBlank { reference.symbol },
            shortName = meta.optString("shortName").ifBlank { reference.displayName },
            currency = meta.optString("currency").ifBlank { null },
            exchangeName = meta.optString("exchangeName").ifBlank { null },
            price = price,
            previousClose = previousClose,
            marketTime = meta.optLong("regularMarketTime").takeIf { it > 0L },
            marketCap = null,
            openPrice = opens?.lastNonNullDouble(),
            dayHigh = highs?.lastNonNullDouble(),
            dayLow = lows?.lastNonNullDouble(),
            chartPoints = if (includeChart) timestamps.toChartPoints(closes) else emptyList()
        )
    }

    private fun JSONArray?.toChartPoints(closes: JSONArray?): List<ChartPoint> {
        if (this == null || closes == null) return emptyList()
        val points = mutableListOf<ChartPoint>()
        val count = minOf(length(), closes.length())
        for (index in 0 until count) {
            if (isNull(index) || closes.isNull(index)) continue
            val timestamp = optLong(index)
            val close = closes.optDouble(index).takeUnless { it.isNaN() } ?: continue
            points += ChartPoint(timestamp = timestamp, close = close)
        }
        return points
    }

    private fun JSONArray.lastNonNullDouble(): Double? {
        for (index in length() - 1 downTo 0) {
            if (!isNull(index)) {
                return optDouble(index).takeUnless { it.isNaN() }
            }
        }
        return null
    }

    private fun JSONArray.previousNonNullDouble(): Double? {
        var foundLatest = false
        for (index in length() - 1 downTo 0) {
            if (!isNull(index)) {
                val value = optDouble(index).takeUnless { it.isNaN() } ?: continue
                if (!foundLatest) {
                    foundLatest = true
                } else {
                    return value
                }
            }
        }
        return null
    }
}
