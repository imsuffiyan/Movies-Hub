package com.example.movieapp.di

import android.content.Context
import com.example.movieapp.network.NetworkModule
import com.example.movieapp.network.TmdbApi
import com.example.movieapp.repository.FavoritesRepository
import com.example.movieapp.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTmdbApi(): TmdbApi = NetworkModule.api

    @Provides
    @Singleton
    fun provideMovieRepository(api: TmdbApi): MovieRepository = MovieRepository(api)

    @Provides
    @Singleton
    fun provideFavoritesRepository(
        @ApplicationContext context: Context,
    ): FavoritesRepository = FavoritesRepository(context)
}

