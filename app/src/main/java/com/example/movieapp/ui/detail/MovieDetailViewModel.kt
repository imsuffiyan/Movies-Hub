package com.example.movieapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.usecase.AddFavoriteUseCase
import com.example.movieapp.domain.usecase.IsFavoriteUseCase
import com.example.movieapp.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
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
            isFavorite = false,
        ),
    )
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MovieDetailEvent>()
    val events: SharedFlow<MovieDetailEvent> = _events.asSharedFlow()

    init {
        if (args.id > 0) {
            viewModelScope.launch {
                val favorite = isFavoriteUseCase(args.id)
                _uiState.update { it.copy(isFavorite = favorite) }
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value
        if (!current.hasValidId) {
            return
        }
        viewModelScope.launch {
            val shouldBeFavorite = !current.isFavorite
            if (shouldBeFavorite) {
                addFavoriteUseCase(current.toMovie())
            } else {
                removeFavoriteUseCase(current.movieId)
            }
            _uiState.update { it.copy(isFavorite = shouldBeFavorite) }
            _events.emit(MovieDetailEvent.FavoriteToggled(shouldBeFavorite))
        }
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

private fun MovieDetailUiState.toMovie(): Movie = Movie(
    id = movieId,
    title = title,
    overview = overview,
    posterPath = posterPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage.takeIf { it >= 0f },
    genreIds = genreIds,
)

sealed interface MovieDetailEvent {
    data class FavoriteToggled(val isFavorite: Boolean) : MovieDetailEvent
}
