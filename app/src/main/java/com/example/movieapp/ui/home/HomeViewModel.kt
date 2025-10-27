package com.example.movieapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.usecase.GetMoviesPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMoviesPageUseCase: GetMoviesPageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            sections = DEFAULT_CATEGORIES.associateWith { SectionState(isLoading = true) },
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        refreshSections()
    }

    fun refreshSections() {
        DEFAULT_CATEGORIES.forEach { category ->
            loadCategory(category)
        }
    }

    private fun loadCategory(category: MovieCategory) {
        viewModelScope.launch {
            _uiState.updateSection(category) { copy(isLoading = true, errorMessage = null) }
            val result = getMoviesPageUseCase(category, page = 1)
            result.onSuccess { page ->
                _uiState.updateSection(category) {
                    copy(movies = page.movies, isLoading = false, errorMessage = null)
                }
            }
            result.onFailure { throwable ->
                val message = throwable.message ?: ""
                _uiState.updateSection(category) {
                    copy(isLoading = false, errorMessage = message)
                }
                _events.emit(HomeEvent.SectionError(category, message))
            }
        }
    }

    private fun MutableStateFlow<HomeUiState>.updateSection(
        category: MovieCategory,
        transform: SectionState.() -> SectionState,
    ) {
        val currentSections = value.sections.toMutableMap()
        val currentState = currentSections[category] ?: SectionState()
        currentSections[category] = currentState.transform()
        value = value.copy(sections = currentSections)
    }

    companion object {
        private val DEFAULT_CATEGORIES = listOf(
            MovieCategory.TOP_RATED,
            MovieCategory.POPULAR,
            MovieCategory.NOW_PLAYING,
        )
    }
}

sealed interface HomeEvent {
    data class SectionError(val category: MovieCategory, val message: String) : HomeEvent
}
