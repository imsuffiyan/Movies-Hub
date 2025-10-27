package com.example.movieapp.data.remote.dto

data class MovieResponseDto(
    val page: Int,
    val results: List<MovieDto> = emptyList(),
    val total_results: Int = 0,
    val total_pages: Int = 0,
)
