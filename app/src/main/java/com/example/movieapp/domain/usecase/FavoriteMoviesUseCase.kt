package com.example.movieapp.domain.usecase

import com.example.movieapp.domain.repository.FavoritesRepositoryInterface
import com.example.movieapp.model.Movie
import javax.inject.Inject

class FavoriteMoviesUseCase @Inject constructor(
    private val repository: FavoritesRepositoryInterface
) {
    fun getAllFavorites(): List<Movie> = repository.getAllFavorites()
    
    fun addToFavorites(movie: Movie) = repository.addToFavorites(movie)
    
    fun removeFromFavorites(movieId: Int) = repository.removeFromFavorites(movieId)
    
    fun isFavorite(movieId: Int): Boolean = repository.isFavorite(movieId)
}