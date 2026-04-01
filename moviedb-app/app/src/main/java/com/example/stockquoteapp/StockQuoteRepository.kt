package com.example.stockquoteapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class StockQuoteRepository(
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun getPopularMovies(): Result<MovieListPayload> = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            return@withContext Result.success(sampleMovieList())
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
            sampleMovieList()
        }
    }

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetailPayload> = withContext(Dispatchers.IO) {
        val sample = sampleMovieDetail(movieId)
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            return@withContext sample?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("Add tmdbApiKey to local.properties to fetch live data."))
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
            sample ?: throw throwable
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
        posterUrl = optString("poster_path").toPosterUrl(),
        backdropUrl = optString("backdrop_path").toBackdropUrl(),
        releaseDate = optString("release_date"),
        rating = optDouble("vote_average")
    )

private fun JSONObject.toMovieDetail(): MovieDetail =
    MovieDetail(
        id = getInt("id"),
        title = optString("title").ifBlank { optString("name") },
        overview = optString("overview"),
        posterUrl = optString("poster_path").toPosterUrl(),
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

private fun String.toPosterUrl(): String? =
    takeIf { it.isNotBlank() }?.let { "https://image.tmdb.org/t/p/w500$it" }

private fun String.toBackdropUrl(): String? =
    takeIf { it.isNotBlank() }?.let { "https://image.tmdb.org/t/p/w780$it" }

private val sampleMovies = listOf(
    MovieDetail(
        id = 603,
        title = "The Matrix",
        overview = "A hacker discovers reality is a simulation and joins a rebellion against the machines.",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = "1999-03-31",
        rating = 8.2,
        genres = listOf("Science Fiction", "Action"),
        runtimeMinutes = 136,
        language = "en",
        tagline = "Reality is a thing of the past.",
        cast = listOf("Keanu Reeves", "Carrie-Anne Moss", "Laurence Fishburne")
    ),
    MovieDetail(
        id = 157336,
        title = "Interstellar",
        overview = "Explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = "2014-11-05",
        rating = 8.4,
        genres = listOf("Adventure", "Drama", "Science Fiction"),
        runtimeMinutes = 169,
        language = "en",
        tagline = "Mankind was born on Earth. It was never meant to die here.",
        cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain")
    ),
    MovieDetail(
        id = 27205,
        title = "Inception",
        overview = "A thief who steals secrets through dream-sharing technology is hired to plant an idea.",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = "2010-07-15",
        rating = 8.4,
        genres = listOf("Action", "Science Fiction", "Adventure"),
        runtimeMinutes = 148,
        language = "en",
        tagline = "Your mind is the scene of the crime.",
        cast = listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page")
    ),
    MovieDetail(
        id = 496243,
        title = "Parasite",
        overview = "A darkly funny thriller about two families drawn into an unstable relationship.",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = "2019-05-30",
        rating = 8.5,
        genres = listOf("Thriller", "Drama", "Comedy"),
        runtimeMinutes = 133,
        language = "ko",
        tagline = "Act like you own the place.",
        cast = listOf("Song Kang-ho", "Lee Sun-kyun", "Cho Yeo-jeong")
    )
)

private fun sampleMovieList(): MovieListPayload =
    MovieListPayload(
        movies = sampleMovies.map {
            MovieSummary(
                id = it.id,
                title = it.title,
                overview = it.overview,
                posterUrl = it.posterUrl,
                backdropUrl = it.backdropUrl,
                releaseDate = it.releaseDate,
                rating = it.rating
            )
        },
        notice = "Showing sample movies because the TMDb API key is missing or the request failed."
    )

private fun sampleMovieDetail(movieId: Int): MovieDetailPayload? =
    sampleMovies.firstOrNull { it.id == movieId }?.let {
        MovieDetailPayload(
            movie = it,
            notice = "Showing sample movie details."
        )
    }
