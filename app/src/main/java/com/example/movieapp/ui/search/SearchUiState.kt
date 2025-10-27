package com.example.movieapp.ui.search

import com.example.movieapp.domain.model.Movie

data class SearchUiState(
    val isLoading: Boolean = false,
    val results: List<Movie> = emptyList(),
    val showInstruction: Boolean = true,
    val showEmptyState: Boolean = false,
)

sealed interface SearchEvent {
    data class Error(val message: String?) : SearchEvent
}
