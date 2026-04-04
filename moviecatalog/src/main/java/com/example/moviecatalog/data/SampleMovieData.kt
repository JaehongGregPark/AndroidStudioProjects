package com.example.moviecatalog.data

object SampleMovieData {
    private val movies = listOf(
        MovieDetail(
            id = 603,
            title = "The Matrix",
            overview = "A cyberpunk classic where Neo discovers the hidden truth behind reality.",
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
            overview = "A team travels across space and time to find a future for humanity.",
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
            overview = "A skilled thief enters layered dreams to plant an idea in a target's mind.",
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
            overview = "Two families from different social worlds become entangled in a tense drama.",
            posterUrl = null,
            backdropUrl = null,
            releaseDate = "2019-05-30",
            rating = 8.5,
            genres = listOf("Thriller", "Drama", "Comedy"),
            runtimeMinutes = 133,
            language = "ko",
            tagline = "Act like you own the place.",
            cast = listOf("Song Kang-ho", "Lee Sun-kyun", "Cho Yeo-jeong")
        ),
        MovieDetail(
            id = 24428,
            title = "The Avengers",
            overview = "Marvel heroes unite to stop a global threat in a blockbuster team-up.",
            posterUrl = null,
            backdropUrl = null,
            releaseDate = "2012-04-25",
            rating = 7.7,
            genres = listOf("Science Fiction", "Action", "Adventure"),
            runtimeMinutes = 143,
            language = "en",
            tagline = "Some assembly required.",
            cast = listOf("Robert Downey Jr.", "Chris Evans", "Scarlett Johansson")
        )
    )

    fun listPayload(): MovieListPayload = MovieListPayload(
        movies = movies.map {
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

    fun detailPayload(id: Int): MovieDetailPayload? =
        movies.firstOrNull { it.id == id }?.let {
            MovieDetailPayload(
                movie = it,
                notice = "Showing sample movie details."
            )
        }
}
