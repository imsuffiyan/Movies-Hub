package com.example.movieapp.domain.repository

import com.example.movieapp.domain.model.Movie

interface FavoritesRepository {
    suspend fun add(movie: Movie)
    suspend fun remove(movieId: Int)
    suspend fun isFavorite(movieId: Int): Boolean
    suspend fun fetchFavorites(): List<Movie>
}
