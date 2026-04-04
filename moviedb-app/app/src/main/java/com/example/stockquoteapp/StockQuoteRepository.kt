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
    suspend fun getPopularMovies(
        countryCode: String,
        languageTag: String
    ): Result<MovieListPayload> = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            return@withContext Result.success(sampleMovieList(countryCode, languageTag))
        }

        runCatching {
            val movies = requestMoviePage(
                countryCode = countryCode,
                page = 1,
                languageTag = languageTag
            ).take(20)

            MovieListPayload(
                movies = movies,
                notice = liveNotice(countryCode, languageTag)
            )
        }.recoverCatching {
            sampleMovieList(countryCode, languageTag)
        }
    }

    suspend fun getMovieDetail(
        movieId: Int,
        languageTag: String
    ): Result<MovieDetailPayload> = withContext(Dispatchers.IO) {
        val sample = sampleMovieDetail(movieId, languageTag)
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            return@withContext sample?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("Add tmdbApiKey to local.properties to fetch live data."))
        }

        runCatching {
            val json = requestJson(
                pathSegments = listOf("3", "movie", movieId.toString()),
                queryParams = mapOf(
                    "language" to languageTag,
                    "append_to_response" to "credits"
                )
            )

            MovieDetailPayload(
                movie = json.toMovieDetail(),
                notice = detailNotice(languageTag)
            )
        }.recoverCatching { throwable ->
            sample ?: throw throwable
        }
    }

    private fun requestMoviePage(
        countryCode: String,
        page: Int,
        languageTag: String
    ): List<MovieSummary> {
        val json = if (countryCode == "ALL") {
            requestJson(
                pathSegments = listOf("3", "movie", "popular"),
                queryParams = mapOf(
                    "language" to languageTag,
                    "page" to page.toString()
                )
            )
        } else {
            requestJson(
                pathSegments = listOf("3", "discover", "movie"),
                queryParams = mapOf(
                    "language" to languageTag,
                    "page" to page.toString(),
                    "sort_by" to "popularity.desc",
                    "include_adult" to "false",
                    "include_video" to "false",
                    "with_origin_country" to countryCode
                )
            )
        }

        return json.getJSONArray("results").toMovieSummaries(countryCode)
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

private fun JSONArray.toMovieSummaries(countryCode: String): List<MovieSummary> =
    buildList {
        for (index in 0 until length()) {
            add(getJSONObject(index).toMovieSummary(countryCode))
        }
    }

private fun JSONObject.toMovieSummary(countryCode: String): MovieSummary =
    MovieSummary(
        id = getInt("id"),
        title = optString("title").ifBlank { optString("name") },
        overview = optString("overview"),
        posterUrl = optString("poster_path").toPosterUrl(),
        backdropUrl = optString("backdrop_path").toBackdropUrl(),
        releaseDate = optString("release_date"),
        rating = optDouble("vote_average"),
        countryCode = countryCode
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
            .orEmpty(),
        countryCode = optJSONArray("production_countries")
            ?.optJSONObject(0)
            ?.optString("iso_3166_1")
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

private fun sampleMovieList(countryCode: String, languageTag: String): MovieListPayload {
    val filtered = sampleMoviesForCountry(countryCode)
    return MovieListPayload(
        movies = filtered.map { movie ->
            localizeMovieDetail(movie, languageTag).toSummary()
        },
        notice = sampleNotice(countryCode, languageTag)
    )
}

private fun sampleMovieDetail(movieId: Int, languageTag: String): MovieDetailPayload? =
    allSampleMovies.firstOrNull { it.id == movieId }?.let {
        MovieDetailPayload(
            movie = localizeMovieDetail(it, languageTag),
            notice = detailNotice(languageTag)
        )
    }

private fun MovieDetail.toSummary(): MovieSummary =
    MovieSummary(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = releaseDate,
        rating = rating,
        countryCode = countryCode
    )

private fun localizeMovieDetail(movie: MovieDetail, languageTag: String): MovieDetail =
    when {
        languageTag.startsWith("ko") -> movie.copy(
            overview = "${movie.title}의 샘플 줄거리입니다. 현재는 라이브 TMDb 데이터를 대신해 표시되고 있습니다.",
            tagline = "${movie.countryCode} 샘플 영화"
        )
        languageTag.startsWith("ja") -> movie.copy(
            overview = "${movie.title} のサンプル概要です。現在は TMDb の代わりに表示されています。",
            tagline = "${movie.countryCode} のサンプル映画"
        )
        languageTag.startsWith("fr") -> movie.copy(
            overview = "Resume de demonstration pour ${movie.title}. Ce contenu apparait lorsque les donnees TMDb ne sont pas disponibles.",
            tagline = "Film exemple ${movie.countryCode}"
        )
        languageTag.startsWith("hi") -> movie.copy(
            overview = "${movie.title} के लिए यह एक सैंपल सारांश है। TMDb डेटा उपलब्ध न होने पर इसे दिखाया जाता है।",
            tagline = "${movie.countryCode} सैंपल मूवी"
        )
        else -> movie
    }

private fun liveNotice(countryCode: String, languageTag: String): String =
    when {
        countryCode == "ALL" -> "Showing about 20 popular movies."
        languageTag.startsWith("ko") -> "${countryLabel(countryCode)} 영화 약 20개를 번역 모드로 표시합니다."
        languageTag.startsWith("ja") -> "${countryLabel(countryCode)} の映画を約20件、翻訳モードで表示します。"
        languageTag.startsWith("fr") -> "Affichage d'environ 20 films de ${countryLabel(countryCode)} en mode traduction."
        languageTag.startsWith("hi") -> "${countryLabel(countryCode)} की लगभग 20 फिल्मों को अनुवाद मोड में दिखाया जा रहा है।"
        else -> "Showing about 20 movies from ${countryLabel(countryCode)}."
    }

private fun sampleNotice(countryCode: String, languageTag: String): String =
    when {
        countryCode == "ALL" -> "Showing 20 sample movies. Open sample-data/sample_movies_by_country.json to view the sample file."
        languageTag.startsWith("ko") -> "${countryLabel(countryCode)} 샘플 영화 20개를 번역 모드로 표시합니다."
        languageTag.startsWith("ja") -> "${countryLabel(countryCode)} のサンプル映画20件を翻訳モードで表示します。"
        languageTag.startsWith("fr") -> "Affichage de 20 films exemples de ${countryLabel(countryCode)} en mode traduction."
        languageTag.startsWith("hi") -> "${countryLabel(countryCode)} की 20 सैंपल फिल्मों को अनुवाद मोड में दिखाया जा रहा है।"
        else -> "Showing 20 sample movies from ${countryLabel(countryCode)}. Open sample-data/sample_movies_by_country.json to view the sample file."
    }

private fun detailNotice(languageTag: String): String =
    when {
        languageTag.startsWith("ko") -> "상세 정보가 선택한 국가 언어 기준으로 표시됩니다."
        languageTag.startsWith("ja") -> "詳細情報は選択した国の言語で表示されます。"
        languageTag.startsWith("fr") -> "Les details sont affiches dans la langue du pays selectionne."
        languageTag.startsWith("hi") -> "विवरण चुने गए देश की भाषा में दिखाया जा रहा है।"
        else -> "Movie details are shown using the selected language."
    }

private fun sampleMoviesForCountry(countryCode: String): List<MovieDetail> =
    if (countryCode == "ALL") {
        allSampleMovies.take(20)
    } else {
        allSampleMovies.filter { it.countryCode == countryCode }.take(20)
    }

private fun buildCountrySamples(
    countryCode: String,
    language: String,
    titles: List<String>,
    genres: List<String>,
    castPool: List<String>,
    startId: Int
): List<MovieDetail> =
    titles.mapIndexed { index, title ->
        MovieDetail(
            id = startId + index,
            title = title,
            overview = "$title is a sample movie for $countryCode used when live TMDb data is unavailable.",
            posterUrl = null,
            backdropUrl = null,
            releaseDate = "20${10 + (index % 10)}-${(index % 12 + 1).toString().padStart(2, '0')}-${(index % 28 + 1).toString().padStart(2, '0')}",
            rating = 6.5 + (index % 4) * 0.4,
            genres = genres.shuffled().take(2),
            runtimeMinutes = 100 + (index % 7) * 8,
            language = language,
            tagline = "Sample release ${index + 1} from $countryCode",
            cast = castPool.shuffled().take(3),
            countryCode = countryCode
        )
    }

private val allSampleMovies: List<MovieDetail> = buildList {
    addAll(
        buildCountrySamples(
            countryCode = "US",
            language = "en",
            titles = listOf(
                "Neon Skyline", "Last Orbit", "Desert Signal", "Maple Street Chase", "Silver Harbor",
                "Broken Satellite", "Night Voltage", "Midtown Heist", "Dust and Thunder", "Glass Horizon",
                "Cinder Lake", "Pulse Runner", "Blue Static", "Terminal Echo", "Marble City",
                "Signal After Dark", "Westbound Fire", "Cloudline", "Zero District", "Golden Relay"
            ),
            genres = listOf("Action", "Thriller", "Drama", "Science Fiction"),
            castPool = listOf("Mason Cole", "Ava Brooks", "Liam Hayes", "Emma Reed", "Noah Price"),
            startId = 1000
        )
    )
    addAll(
        buildCountrySamples(
            countryCode = "KR",
            language = "ko",
            titles = listOf(
                "Seoul Midnight", "Han River Signal", "Winter Station", "Shadow Market", "Blue Alley",
                "Mirror District", "Silent Monsoon", "Northern Lights Seoul", "Underpass Story", "Ginkgo Road",
                "Echoes of Busan", "Late Train Home", "Crimson Rooftop", "Paper Lantern Night", "Last Spring Rain",
                "Stone Bridge", "Signal in Incheon", "Hidden Classroom", "Moon Over Jeju", "South Gate"
            ),
            genres = listOf("Drama", "Thriller", "Mystery", "Romance"),
            castPool = listOf("Kim Min-seo", "Park Ji-hoon", "Lee Soo-jin", "Choi Yuna", "Jung Hae-min"),
            startId = 2000
        )
    )
    addAll(
        buildCountrySamples(
            countryCode = "JP",
            language = "ja",
            titles = listOf(
                "Tokyo Reverie", "Glass Garden", "Summer Platform", "Quiet Lantern", "Midnight Bento",
                "Paper Crane Sky", "Rain Over Shibuya", "Ocean Tram", "Snow Bell", "Neon Shrine",
                "Autumn Signal", "Whispering Alley", "Kobe Harbor Song", "Static Sakura", "Cobalt Train",
                "Moonlit Arcade", "Horizon Postcard", "Velvet Crossing", "Yokohama Blue", "Fourth Season"
            ),
            genres = listOf("Animation", "Drama", "Fantasy", "Romance"),
            castPool = listOf("Haru Sato", "Yui Arai", "Ren Takahashi", "Mio Kanda", "Daiki Mori"),
            startId = 3000
        )
    )
    addAll(
        buildCountrySamples(
            countryCode = "FR",
            language = "fr",
            titles = listOf(
                "Paris After Rain", "Velvet Metro", "Blue Cafe", "Rue des Lumieres", "Harbor of Marseille",
                "Autumn Window", "Silent Balcony", "Cinema du Matin", "Ivory Street", "Lyon Twilight",
                "Golden Bicycle", "Midnight on the Seine", "Crimson Letterbox", "Second Summer", "Candles in Nice",
                "Rose Apartment", "Montmartre Static", "Paper Ticket", "Winter in Bordeaux", "Fading Polaroid"
            ),
            genres = listOf("Drama", "Romance", "Comedy", "Mystery"),
            castPool = listOf("Camille Laurent", "Louis Moreau", "Chloe Martin", "Theo Bernard", "Nina Petit"),
            startId = 4000
        )
    )
    addAll(
        buildCountrySamples(
            countryCode = "IN",
            language = "hi",
            titles = listOf(
                "Monsoon City", "Red Fort Echo", "Northern Caravan", "Festival Lights", "Mumbai Pulse",
                "River of Jasmine", "Hidden Courtyard", "Lotus Signal", "Skyline Rickshaw", "Cricket Street",
                "Midnight Bazaar", "Saffron Dust", "Falling Rangoli", "Golden Monsoon", "Chennai Horizon",
                "Palace Lantern", "Songs of Jaipur", "Blue Temple Road", "Desert Monsoon", "Final Procession"
            ),
            genres = listOf("Drama", "Action", "Romance", "History"),
            castPool = listOf("Arjun Mehta", "Priya Kapoor", "Kabir Anand", "Isha Verma", "Rohan Malhotra"),
            startId = 5000
        )
    )
    addAll(
        buildCountrySamples(
            countryCode = "GB",
            language = "en",
            titles = listOf(
                "London Static", "North Sea Signal", "Ashford Lane", "Rain on Baker Street", "The Quiet Crown",
                "Velvet Albion", "Glass Parliament", "Manchester Blue", "Sunday Platform", "Red Telephone Box",
                "Night Ferry", "Coal and Snow", "Westminster After Dark", "Silver Underground", "Cold Harbor",
                "Midlands Echo", "Stonebridge Case", "Second Tea", "Fogline", "The Final Broadcast"
            ),
            genres = listOf("Drama", "Crime", "Mystery", "History"),
            castPool = listOf("Oliver Grant", "Amelia Scott", "Henry Clarke", "Isla Turner", "George Hayes"),
            startId = 6000
        )
    )
}

private fun countryLabel(countryCode: String): String =
    countryFilters.firstOrNull { it.code == countryCode }?.label ?: countryCode
