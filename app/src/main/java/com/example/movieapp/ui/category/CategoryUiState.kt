package com.example.movieapp.ui.category

import com.example.movieapp.model.Movie

data class CategoryUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val category: String = "",
    val title: String = ""
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = movies.isEmpty() && !isLoading
}