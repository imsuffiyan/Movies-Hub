package com.example.movieapp.data.mapper

import com.example.movieapp.data.remote.dto.MovieDto
import com.example.movieapp.data.remote.dto.MovieResponseDto
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MoviePage

fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    genreIds = genreIds.orEmpty(),
)

fun MovieResponseDto.toDomain(): MoviePage = MoviePage(
    page = page,
    movies = results.map(MovieDto::toDomain),
    totalPages = total_pages,
)
