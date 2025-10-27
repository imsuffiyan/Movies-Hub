package com.example.movieapp.data.remote.source

import com.example.movieapp.BuildConfig
import com.example.movieapp.data.remote.api.TmdbApi
import com.example.movieapp.data.remote.api.awaitResponse
import com.example.movieapp.data.remote.dto.MovieResponseDto
import com.example.movieapp.domain.model.MovieCategory
import javax.inject.Inject
import retrofit2.HttpException

class MovieRemoteDataSource @Inject constructor(
    private val api: TmdbApi,
) {
    private fun requireApiKey(): String {
        return BuildConfig.TMDB_API_KEY.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("TMDB API key is missing. Please configure it as described in the README.")
    }

    suspend fun searchMovies(query: String): MovieResponseDto {
        val response = api.searchMovies(requireApiKey(), query).awaitResponse()
        if (response.isSuccessful) {
            return response.body() ?: MovieResponseDto(1, emptyList(), 0, 0)
        }
        throw HttpException(response)
    }

    suspend fun fetchMovies(category: MovieCategory, page: Int): MovieResponseDto {
        val key = requireApiKey()
        val call = when (category) {
            MovieCategory.TOP_RATED -> api.getTopRated(key, page)
            MovieCategory.NOW_PLAYING -> api.getNowPlaying(key, page)
            else -> api.getPopular(key, page)
        }
        val response = call.awaitResponse()
        if (response.isSuccessful) {
            return response.body() ?: MovieResponseDto(page, emptyList(), 0, 0)
        }
        throw HttpException(response)
    }
}
