package com.example.movieapp.domain.repository

import com.example.movieapp.model.Movie

interface MovieRepositoryInterface {
    suspend fun getTopRated(): Result<List<Movie>>
    suspend fun getPopular(): Result<List<Movie>>
    suspend fun getNowPlaying(): Result<List<Movie>>
    suspend fun searchMovies(query: String): Result<List<Movie>>
}