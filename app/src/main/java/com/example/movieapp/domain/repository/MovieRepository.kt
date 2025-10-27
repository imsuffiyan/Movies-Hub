package com.example.movieapp.domain.repository

import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.model.MoviePage

interface MovieRepository {
    suspend fun fetchCategoryPage(category: MovieCategory, page: Int = 1): MoviePage
    suspend fun searchMovies(query: String): List<Movie>
}
