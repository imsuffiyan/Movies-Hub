package com.example.movieapp.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.usecase.IsFavoriteUseCase
import com.example.movieapp.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
) : ViewModel() {

    private val args = MovieDetailFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _uiState = MutableStateFlow(
        MovieDetailUiState(
            movieId = args.id,
            title = args.title,
            overview = args.overview,
            posterPath = args.poster,
            releaseDate = args.releaseDate,
            voteAverage = args.voteAverage,
            genreIds = args.genreIds?.toList().orEmpty(),
        ),
    )
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        refreshFavoriteState()
    }

    suspend fun toggleFavorite(): Boolean? {
        val current = _uiState.value
        if (!current.hasValidId) return null
        val movie = current.toMovie()
        val isFavorite = toggleFavoriteUseCase(movie)
        _uiState.update { it.copy(isFavorite = isFavorite) }
        return isFavorite
    }

    fun refreshFavoriteState() {
        val current = _uiState.value
        if (!current.hasValidId) return
        viewModelScope.launch {
            val isFavorite = isFavoriteUseCase(current.movieId).getOrDefault(false)
            _uiState.update { it.copy(isFavorite = isFavorite) }
        }
    }

    private fun MovieDetailUiState.toMovie(): Movie {
        return Movie(
            id = movieId,
            title = title,
            overview = overview,
            posterPath = posterPath,
            releaseDate = releaseDate,
            voteAverage = voteAverage.takeIf { it >= 0f },
            genreIds = genreIds.takeIf { it.isNotEmpty() } ?: emptyList(),
        )
    }
}

data class MovieDetailUiState(
    val movieId: Int = -1,
    val title: String? = null,
    val overview: String? = null,
    val posterPath: String? = null,
    val releaseDate: String? = null,
    val voteAverage: Float = -1f,
    val genreIds: List<Int> = emptyList(),
    val isFavorite: Boolean = false,
) {
    val hasValidId: Boolean
        get() = movieId > 0
}
