package com.example.movieapp.di

import android.content.Context
import com.example.movieapp.core.di.IoDispatcher
import com.example.movieapp.data.local.FavoritesLocalDataSource
import com.example.movieapp.data.remote.api.NetworkModule
import com.example.movieapp.data.remote.api.TmdbApi
import com.example.movieapp.data.repository.FavoritesRepositoryImpl
import com.example.movieapp.data.repository.MovieRepositoryImpl
import com.example.movieapp.domain.repository.FavoritesRepository
import com.example.movieapp.domain.repository.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTmdbApi(): TmdbApi = NetworkModule.api

    @Provides
    @Singleton
    fun provideFavoritesLocalDataSource(
        @ApplicationContext context: Context,
    ): FavoritesLocalDataSource = FavoritesLocalDataSource(context)

    @Provides
    @Singleton
    fun provideMovieRepository(
        api: TmdbApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): MovieRepository = MovieRepositoryImpl(api, ioDispatcher)

    @Provides
    @Singleton
    fun provideFavoritesRepository(
        localDataSource: FavoritesLocalDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): FavoritesRepository = FavoritesRepositoryImpl(localDataSource, ioDispatcher)
}
