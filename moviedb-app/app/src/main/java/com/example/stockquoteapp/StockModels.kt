package com.example.stockquoteapp

data class MovieSummary(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val rating: Double
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val rating: Double,
    val genres: List<String>,
    val runtimeMinutes: Int?,
    val language: String,
    val tagline: String,
    val cast: List<String>
)

data class MovieListPayload(
    val movies: List<MovieSummary>,
    val notice: String? = null
)

data class MovieDetailPayload(
    val movie: MovieDetail,
    val notice: String? = null
)
