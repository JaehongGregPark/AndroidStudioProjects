package com.example.stockquoteapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieUiState(
    val isLoading: Boolean = true,
    val movies: List<MovieSummary> = emptyList(),
    val selectedMovie: MovieDetail? = null,
    val isDetailLoading: Boolean = false,
    val notice: String? = null,
    val error: String? = null
)

class StockQuoteViewModel(
    private val repository: StockQuoteRepository = StockQuoteRepository()
) : ViewModel() {
    private val detailCache = mutableMapOf<Int, MovieDetail>()

    private val _uiState = MutableStateFlow(MovieUiState())
    val uiState: StateFlow<MovieUiState> = _uiState.asStateFlow()

    init {
        refreshMovies()
    }

    fun refreshMovies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getPopularMovies()
                .onSuccess { payload ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movies = payload.movies,
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
                            error = throwable.message ?: "Could not load movies."
                        )
                    }
                }
        }
    }

    fun loadMovieDetail(movieId: Int) {
        detailCache[movieId]?.let { cached ->
            _uiState.update {
                it.copy(selectedMovie = cached, isDetailLoading = false, error = null)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(selectedMovie = null, isDetailLoading = true, error = null) }

            repository.getMovieDetail(movieId)
                .onSuccess { payload ->
                    detailCache[movieId] = payload.movie
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
