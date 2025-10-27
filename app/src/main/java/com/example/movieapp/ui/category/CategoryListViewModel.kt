package com.example.movieapp.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.usecase.FavoriteMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val favoriteMoviesUseCase: FavoriteMoviesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    fun initialize(category: String, title: String) {
        _uiState.value = _uiState.value.copy(category = category, title = title)
        loadMovies()
    }

    fun loadMovies() {
        val currentState = _uiState.value
        if (currentState.category.isEmpty()) return

        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                when (currentState.category) {
                    "favorite" -> {
                        val favorites = favoriteMoviesUseCase.getAllFavorites()
                        _uiState.value = currentState.copy(
                            movies = favorites,
                            isLoading = false
                        )
                    }
                    else -> {
                        // For other categories, we'll use the existing paging adapter
                        _uiState.value = currentState.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun refreshFavorites() {
        if (_uiState.value.category == "favorite") {
            loadMovies()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}