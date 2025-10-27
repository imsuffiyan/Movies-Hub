package com.example.movieapp.domain.usecase

import com.example.movieapp.domain.repository.MovieRepositoryInterface
import com.example.movieapp.model.Movie
import javax.inject.Inject

class GetMoviesUseCase @Inject constructor(
    private val repository: MovieRepositoryInterface
) {
    suspend fun getTopRated(): Result<List<Movie>> = repository.getTopRated()
    
    suspend fun getPopular(): Result<List<Movie>> = repository.getPopular()
    
    suspend fun getNowPlaying(): Result<List<Movie>> = repository.getNowPlaying()
}