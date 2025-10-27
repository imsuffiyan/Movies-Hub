package com.example.movieapp.data.repository

import com.example.movieapp.data.remote.source.MovieRemoteDataSource
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.model.MoviePage
import com.example.movieapp.domain.repository.MovieRepository
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val remoteDataSource: MovieRemoteDataSource,
) : MovieRepository {

    override suspend fun searchMovies(query: String): Result<List<Movie>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return runCatching {
            val response = remoteDataSource.searchMovies(query)
            response.results.map { it.toDomain() }
        }
    }

    override suspend fun fetchMovies(category: MovieCategory, page: Int): Result<MoviePage> {
        return runCatching {
            remoteDataSource.fetchMovies(category, page).toDomain()
        }
    }
}
