package com.example.movieapp.domain.repository

import com.example.movieapp.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<Movie>>

    suspend fun addFavorite(movie: Movie)

    suspend fun removeFavorite(movieId: Int)

    suspend fun isFavorite(movieId: Int): Boolean
}
