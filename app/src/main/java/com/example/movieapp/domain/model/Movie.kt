package com.example.movieapp.domain.model

data class Movie(
    val id: Int,
    val title: String?,
    val overview: String?,
    val posterPath: String?,
    val releaseDate: String?,
    val voteAverage: Float?,
    val genreIds: List<Int> = emptyList(),
)
