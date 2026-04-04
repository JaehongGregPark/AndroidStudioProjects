package com.example.moviecatalog.data

import com.example.moviecatalog.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class MovieRepository(
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun getPopularMovies(): Result<MovieListPayload> = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            return@withContext Result.success(SampleMovieData.listPayload())
        }

        runCatching {
            val json = requestJson(
                pathSegments = listOf("3", "movie", "popular"),
                queryParams = mapOf(
                    "language" to "en-US",
                    "page" to "1"
                )
            )

            MovieListPayload(
                movies = json.getJSONArray("results").toMovieSummaries()
            )
        }.recoverCatching {
            SampleMovieData.listPayload()
        }
    }

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetailPayload> = withContext(Dispatchers.IO) {
        SampleMovieData.detailPayload(movieId)?.let { sample ->
            if (BuildConfig.TMDB_API_KEY.isBlank()) {
                return@withContext Result.success(sample)
            }
        }

        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Movie not found in sample data. Add tmdbApiKey to local.properties.")
            )
        }

        runCatching {
            val json = requestJson(
                pathSegments = listOf("3", "movie", movieId.toString()),
                queryParams = mapOf(
                    "language" to "en-US",
                    "append_to_response" to "credits"
                )
            )

            MovieDetailPayload(movie = json.toMovieDetail())
        }.recoverCatching { throwable ->
            SampleMovieData.detailPayload(movieId) ?: throw throwable
        }
    }

    private fun requestJson(
        pathSegments: List<String>,
        queryParams: Map<String, String>
    ): JSONObject {
        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("api.themoviedb.org")

        pathSegments.forEach(urlBuilder::addPathSegment)
        queryParams.forEach { (key, value) -> urlBuilder.addQueryParameter(key, value) }
        urlBuilder.addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)

        val request = Request.Builder()
            .url(urlBuilder.build())
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("TMDb request failed with ${response.code}")
            }

            val body = response.body?.string().orEmpty()
            if (body.isBlank()) {
                error("TMDb response body was empty")
            }

            return JSONObject(body)
        }
    }
}

private fun JSONArray.toMovieSummaries(): List<MovieSummary> =
    buildList {
        for (index in 0 until length()) {
            add(getJSONObject(index).toMovieSummary())
        }
    }

private fun JSONObject.toMovieSummary(): MovieSummary =
    MovieSummary(
        id = getInt("id"),
        title = optString("title").ifBlank { optString("name") },
        overview = optString("overview"),
        posterUrl = optString("poster_path").toImageUrl(),
        backdropUrl = optString("backdrop_path").toBackdropUrl(),
        releaseDate = optString("release_date"),
        rating = optDouble("vote_average")
    )

private fun JSONObject.toMovieDetail(): MovieDetail =
    MovieDetail(
        id = getInt("id"),
        title = optString("title").ifBlank { optString("name") },
        overview = optString("overview"),
        posterUrl = optString("poster_path").toImageUrl(),
        backdropUrl = optString("backdrop_path").toBackdropUrl(),
        releaseDate = optString("release_date"),
        rating = optDouble("vote_average"),
        genres = optJSONArray("genres")?.toNameList().orEmpty(),
        runtimeMinutes = optInt("runtime").takeIf { it > 0 },
        language = optString("original_language"),
        tagline = optString("tagline"),
        cast = optJSONObject("credits")
            ?.optJSONArray("cast")
            ?.toNameList(limit = 6)
            .orEmpty()
    )

private fun JSONArray.toNameList(limit: Int = Int.MAX_VALUE): List<String> =
    buildList {
        for (index in 0 until minOf(length(), limit)) {
            val value = getJSONObject(index).optString("name")
            if (value.isNotBlank()) {
                add(value)
            }
        }
    }

private fun String.toImageUrl(): String? =
    takeIf { it.isNotBlank() }?.let { "https://image.tmdb.org/t/p/w500$it" }

private fun String.toBackdropUrl(): String? =
    takeIf { it.isNotBlank() }?.let { "https://image.tmdb.org/t/p/w780$it" }
