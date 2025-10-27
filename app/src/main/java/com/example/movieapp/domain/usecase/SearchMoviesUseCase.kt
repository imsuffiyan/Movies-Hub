package com.example.movieapp.domain.usecase

import com.example.movieapp.domain.repository.MovieRepositoryInterface
import com.example.movieapp.model.Movie
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val repository: MovieRepositoryInterface
) {
    suspend fun searchMovies(query: String): Result<List<Movie>> = repository.searchMovies(query)
}