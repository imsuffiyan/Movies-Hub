package com.example.movieapp.data.repository

import com.example.movieapp.data.remote.dto.MovieDto
import com.example.movieapp.data.remote.dto.MovieResponseDto
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MoviePage

internal fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    genreIds = genreIds.orEmpty(),
)

internal fun MovieResponseDto.toDomain(): MoviePage = MoviePage(
    page = page,
    movies = results.map { it.toDomain() },
    totalPages = totalPages,
)
