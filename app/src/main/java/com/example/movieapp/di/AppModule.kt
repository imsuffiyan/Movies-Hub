package com.example.movieapp.di

import com.example.movieapp.domain.repository.FavoritesRepositoryInterface
import com.example.movieapp.domain.repository.MovieRepositoryInterface
import com.example.movieapp.network.NetworkModule
import com.example.movieapp.network.TmdbApi
import com.example.movieapp.repository.FavoritesRepository
import com.example.movieapp.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkProviderModule {
    @Provides
    @Singleton
    fun provideTmdbApi(): TmdbApi = NetworkModule.api
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMovieRepository(
        movieRepository: MovieRepository
    ): MovieRepositoryInterface

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        favoritesRepository: FavoritesRepository
    ): FavoritesRepositoryInterface
}

