package com.example.movieapp.domain.repository

import com.example.movieapp.model.Movie

interface FavoritesRepositoryInterface {
    fun getAllFavorites(): List<Movie>
    fun addToFavorites(movie: Movie)
    fun removeFromFavorites(movieId: Int)
    fun isFavorite(movieId: Int): Boolean
}