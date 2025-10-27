package com.example.movieapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.movieapp.domain.usecase.FavoriteMoviesUseCase
import com.example.movieapp.model.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val favoriteMoviesUseCase: FavoriteMoviesUseCase,
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
            isFavorite = if (args.id > 0) favoriteMoviesUseCase.isFavorite(args.id) else false,
        ),
    )
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    fun toggleFavorite(): Boolean? {
        val current = _uiState.value
        if (!current.hasValidId) {
            return null
        }
        val shouldBeFavorite = !current.isFavorite
        if (shouldBeFavorite) {
            val movie = Movie(
                id = current.movieId,
                title = current.title,
                overview = current.overview,
                posterPath = current.posterPath,
                releaseDate = current.releaseDate,
                voteAverage = current.voteAverage.takeIf { it >= 0f },
                genreIds = current.genreIds.takeIf { it.isNotEmpty() },
            )
            favoriteMoviesUseCase.addToFavorites(movie)
        } else {
            favoriteMoviesUseCase.removeFromFavorites(current.movieId)
        }
        _uiState.update { it.copy(isFavorite = shouldBeFavorite) }
        return shouldBeFavorite
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
