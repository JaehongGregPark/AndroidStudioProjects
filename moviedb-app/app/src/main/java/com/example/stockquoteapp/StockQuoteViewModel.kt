package com.example.stockquoteapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val countryFilters = listOf(
    CountryFilter("ALL", "All", "en-US", "Translate"),
    CountryFilter("US", "USA", "en-US", "Translate to English"),
    CountryFilter("KR", "Korea", "ko-KR", "한국어 번역"),
    CountryFilter("JP", "Japan", "ja-JP", "日本語に翻訳"),
    CountryFilter("FR", "France", "fr-FR", "Traduire en francais"),
    CountryFilter("IN", "India", "hi-IN", "हिंदी में अनुवाद"),
    CountryFilter("GB", "UK", "en-GB", "Translate to English")
)

data class MovieUiState(
    val isLoading: Boolean = true,
    val movies: List<MovieSummary> = emptyList(),
    val selectedMovie: MovieDetail? = null,
    val isDetailLoading: Boolean = false,
    val selectedCountryCode: String = "ALL",
    val translationEnabled: Boolean = false,
    val notice: String? = null,
    val error: String? = null
)

class StockQuoteViewModel(
    private val repository: StockQuoteRepository = StockQuoteRepository()
) : ViewModel() {
    private val detailCache = mutableMapOf<String, MovieDetail>()

    private val _uiState = MutableStateFlow(MovieUiState())
    val uiState: StateFlow<MovieUiState> = _uiState.asStateFlow()

    init {
        refreshMovies()
    }

    fun refreshMovies() {
        fetchMovies(
            countryCode = _uiState.value.selectedCountryCode,
            translationEnabled = _uiState.value.translationEnabled
        )
    }

    fun selectCountry(countryCode: String) {
        if (_uiState.value.selectedCountryCode == countryCode) return
        val translationEnabled = if (countryCode == "ALL") false else _uiState.value.translationEnabled
        fetchMovies(countryCode = countryCode, translationEnabled = translationEnabled)
    }

    fun toggleTranslation() {
        val currentCountryCode = _uiState.value.selectedCountryCode
        if (currentCountryCode == "ALL") return
        fetchMovies(
            countryCode = currentCountryCode,
            translationEnabled = !_uiState.value.translationEnabled
        )
    }

    private fun fetchMovies(countryCode: String, translationEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedCountryCode = countryCode,
                    translationEnabled = translationEnabled,
                    error = null
                )
            }

            repository.getPopularMovies(
                countryCode = countryCode,
                languageTag = languageFor(countryCode, translationEnabled)
            )
                .onSuccess { payload ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movies = payload.movies,
                            selectedCountryCode = countryCode,
                            translationEnabled = translationEnabled,
                            notice = payload.notice,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movies = emptyList(),
                            selectedCountryCode = countryCode,
                            translationEnabled = translationEnabled,
                            error = throwable.message ?: "Could not load movies."
                        )
                    }
                }
        }
    }

    fun loadMovieDetail(movieId: Int) {
        val cacheKey = "$movieId|${languageFor(_uiState.value.selectedCountryCode, _uiState.value.translationEnabled)}"

        detailCache[cacheKey]?.let { cached ->
            _uiState.update {
                it.copy(selectedMovie = cached, isDetailLoading = false, error = null)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(selectedMovie = null, isDetailLoading = true, error = null) }

            repository.getMovieDetail(
                movieId = movieId,
                languageTag = languageFor(_uiState.value.selectedCountryCode, _uiState.value.translationEnabled)
            )
                .onSuccess { payload ->
                    detailCache[cacheKey] = payload.movie
                    _uiState.update {
                        it.copy(
                            selectedMovie = payload.movie,
                            isDetailLoading = false,
                            notice = payload.notice ?: it.notice,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isDetailLoading = false,
                            error = throwable.message ?: "Could not load movie details."
                        )
                    }
                }
        }
    }
}

fun languageFor(countryCode: String, translationEnabled: Boolean): String =
    if (!translationEnabled) {
        "en-US"
    } else {
        countryFilters.firstOrNull { it.code == countryCode }?.languageTag ?: "en-US"
    }

fun filterFor(countryCode: String): CountryFilter =
    countryFilters.firstOrNull { it.code == countryCode } ?: countryFilters.first()
