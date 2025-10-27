package com.example.movieapp.domain.model

data class MoviePage(
    val page: Int,
    val movies: List<Movie>,
    val totalPages: Int,
)
