package com.example.movieapp.model

data class Section(
    val title: String,
    val category: String,
    var movies: List<Movie> = emptyList()
)

