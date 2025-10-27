package com.example.movieapp.di

import com.example.movieapp.data.local.FavoritesRepositoryImpl
import com.example.movieapp.data.repository.MovieRepositoryImpl
import com.example.movieapp.domain.repository.FavoritesRepository
import com.example.movieapp.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository
}
