package com.example.movieapp.domain.repository

import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.model.MoviePage

interface MovieRepository {
    suspend fun searchMovies(query: String): Result<List<Movie>>

    suspend fun fetchMovies(category: MovieCategory, page: Int): Result<MoviePage>
}
