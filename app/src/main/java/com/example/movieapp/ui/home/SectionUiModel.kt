package com.example.movieapp.ui.home

import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory

data class SectionUiModel(
    val title: String,
    val category: MovieCategory,
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
