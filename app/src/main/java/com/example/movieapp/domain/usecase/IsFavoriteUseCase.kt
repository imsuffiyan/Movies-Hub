package com.example.movieapp.domain.usecase

import com.example.movieapp.domain.repository.FavoritesRepository
import javax.inject.Inject

class IsFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(movieId: Int): Result<Boolean> = runCatching {
        if (movieId <= 0) {
            false
        } else {
            repository.isFavorite(movieId)
        }
    }
}
