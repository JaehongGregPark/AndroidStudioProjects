package com.example.moviecatalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviecatalog.data.MovieDetail
import com.example.moviecatalog.data.MovieRepository
import com.example.moviecatalog.data.MovieSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieUiState(
    val isLoading: Boolean = true,
    val movies: List<MovieSummary> = emptyList(),
    val selectedMovieId: Int? = null,
    val selectedMovie: MovieDetail? = null,
    val isDetailLoading: Boolean = false,
    val notice: String? = null,
    val error: String? = null
)

class MovieViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {
    private val detailCache = mutableMapOf<Int, MovieDetail>()

    private val _uiState = MutableStateFlow(MovieUiState())
    val uiState: StateFlow<MovieUiState> = _uiState.asStateFlow()

    init {
        refreshMovies()
    }

    fun refreshMovies() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

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
                            error = throwable.message ?: "Could not load the movie list."
                        )
                    }
                }
        }
    }

    fun loadMovieDetail(movieId: Int) {
        if (_uiState.value.selectedMovieId == movieId && (_uiState.value.selectedMovie != null || _uiState.value.isDetailLoading)) {
            return
        }

        detailCache[movieId]?.let { cached ->
            _uiState.update {
                it.copy(
                    selectedMovieId = movieId,
                    selectedMovie = cached,
                    isDetailLoading = false,
                    error = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedMovieId = movieId,
                    selectedMovie = null,
                    isDetailLoading = true,
                    error = null
                )
            }

            repository.getMovieDetail(movieId)
                .onSuccess { payload ->
                    detailCache[movieId] = payload.movie
                    _uiState.update {
                        it.copy(
                            selectedMovieId = movieId,
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
