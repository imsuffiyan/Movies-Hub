package com.example.movieapp.domain.usecase

import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.model.MoviePage
import com.example.movieapp.domain.repository.MovieRepository
import javax.inject.Inject

class GetMoviesPageUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(category: MovieCategory, page: Int): Result<MoviePage> {
        return repository.fetchMovies(category, page)
    }
}
