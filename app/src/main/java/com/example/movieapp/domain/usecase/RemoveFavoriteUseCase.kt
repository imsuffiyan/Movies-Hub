package com.example.movieapp.domain.usecase

import com.example.movieapp.domain.repository.FavoritesRepository
import javax.inject.Inject

class RemoveFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(movieId: Int) {
        repository.removeFavorite(movieId)
    }
}
