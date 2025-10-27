package com.example.movieapp.data.repository

import com.example.movieapp.core.di.IoDispatcher
import com.example.movieapp.data.local.FavoritesLocalDataSource
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.repository.FavoritesRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl @Inject constructor(
    private val localDataSource: FavoritesLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FavoritesRepository {
    override suspend fun add(movie: Movie) = withContext(ioDispatcher) {
        localDataSource.add(movie)
    }

    override suspend fun remove(movieId: Int) = withContext(ioDispatcher) {
        localDataSource.remove(movieId)
    }

    override suspend fun isFavorite(movieId: Int): Boolean = withContext(ioDispatcher) {
        localDataSource.isFavorite(movieId)
    }

    override suspend fun fetchFavorites(): List<Movie> = withContext(ioDispatcher) {
        localDataSource.allFavorites()
    }
}
