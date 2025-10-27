package com.example.movieapp.ui.home

import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory

data class HomeUiState(
    val sections: Map<MovieCategory, SectionState> = emptyMap(),
)

data class SectionState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
