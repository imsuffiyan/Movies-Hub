package com.example.movieapp.ui.search

import com.example.movieapp.model.Movie

data class SearchUiState(
    val query: String = "",
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = movies.isEmpty() && !isLoading && query.isNotBlank()
    val showEmptyState: Boolean get() = movies.isEmpty() && !isLoading && query.isBlank()
}