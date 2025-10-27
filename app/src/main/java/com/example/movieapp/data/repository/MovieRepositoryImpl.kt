package com.example.movieapp.data.repository

import com.example.movieapp.BuildConfig
import com.example.movieapp.core.di.IoDispatcher
import com.example.movieapp.core.network.awaitResponse
import com.example.movieapp.data.mapper.toDomain
import com.example.movieapp.data.remote.api.TmdbApi
import com.example.movieapp.data.remote.dto.MovieResponseDto
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.model.MoviePage
import com.example.movieapp.domain.repository.MovieRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response

class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MovieRepository {

    private fun requireApiKey(): String {
        return BuildConfig.TMDB_API_KEY.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("TMDB API key is missing. Please configure it as described in the README.")
    }

    private suspend fun execute(call: Call<MovieResponseDto>, page: Int): MoviePage = withContext(ioDispatcher) {
        val response: Response<MovieResponseDto> = call.awaitResponse()
        if (response.isSuccessful) {
            val body = response.body() ?: MovieResponseDto(page)
            return@withContext body.toDomain()
        }
        throw HttpException(response)
    }

    override suspend fun fetchCategoryPage(category: MovieCategory, page: Int): MoviePage {
        if (category == MovieCategory.FAVORITE) {
            throw UnsupportedOperationException("Favorite movies are handled by the favorites repository")
        }
        val apiKey = requireApiKey()
        val call = when (category) {
            MovieCategory.TOP_RATED -> api.getTopRated(apiKey, page)
            MovieCategory.NOW_PLAYING -> api.getNowPlaying(apiKey, page)
            MovieCategory.POPULAR -> api.getPopular(apiKey, page)
            MovieCategory.FAVORITE -> error("Unreachable")
        }
        return execute(call, page)
    }

    override suspend fun searchMovies(query: String): List<Movie> = withContext(ioDispatcher) {
        val apiKey = requireApiKey()
        val response = api.searchMovies(apiKey, query).awaitResponse()
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        response.body()?.results?.map { it.toDomain() } ?: emptyList()
    }
}
