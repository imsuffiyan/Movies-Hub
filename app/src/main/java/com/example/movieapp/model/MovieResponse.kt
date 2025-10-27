package com.example.movieapp.model

data class MovieResponse(
    val page: Int,
    val results: List<Movie> = emptyList(),
    val total_results: Int = 0,
    val total_pages: Int = 0
)

