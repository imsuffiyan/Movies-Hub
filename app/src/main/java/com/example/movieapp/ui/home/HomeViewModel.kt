package com.example.movieapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.usecase.GetMoviesUseCase
import com.example.movieapp.model.Section
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMoviesUseCase: GetMoviesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val initialSections = listOf(
        Section(title = "Top Rated Movies", category = "top_rated"),
        Section(title = "Popular Movies", category = "popular"),
        Section(title = "Now Playing", category = "now_playing")
    )

    init {
        _uiState.value = _uiState.value.copy(sections = initialSections)
        loadAllSections()
    }

    fun loadAllSections() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                loadSection("top_rated")
                loadSection("popular") 
                loadSection("now_playing")
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun retrySection(category: String) {
        viewModelScope.launch {
            loadSection(category)
        }
    }

    private suspend fun loadSection(category: String) {
        try {
            val result = when (category) {
                "top_rated" -> getMoviesUseCase.getTopRated()
                "popular" -> getMoviesUseCase.getPopular()
                "now_playing" -> getMoviesUseCase.getNowPlaying()
                else -> return
            }

            result.fold(
                onSuccess = { movies ->
                    updateSectionMovies(category, movies)
                },
                onFailure = { error ->
                    // Handle individual section errors without affecting others
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load $category: ${error.message}"
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to load $category: ${e.message}"
            )
        }
    }

    private fun updateSectionMovies(category: String, movies: List<com.example.movieapp.model.Movie>) {
        val currentSections = _uiState.value.sections.toMutableList()
        val sectionIndex = currentSections.indexOfFirst { it.category == category }
        
        if (sectionIndex >= 0) {
            currentSections[sectionIndex] = currentSections[sectionIndex].copy(movies = movies)
            _uiState.value = _uiState.value.copy(sections = currentSections)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}