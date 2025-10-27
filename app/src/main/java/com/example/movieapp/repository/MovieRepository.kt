package com.example.movieapp.repository

import com.example.movieapp.BuildConfig
import com.example.movieapp.domain.repository.MovieRepositoryInterface
import com.example.movieapp.model.Movie
import com.example.movieapp.model.MovieResponse
import com.example.movieapp.network.TmdbApi
import com.example.movieapp.network.awaitResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume

class MovieRepository @Inject constructor(
    private val api: TmdbApi
) : MovieRepositoryInterface {

    private fun requireApiKey(): String {
        return BuildConfig.TMDB_API_KEY.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("TMDB API key is missing. Please configure it as described in the README.")
    }

    private fun Call<MovieResponse>.enqueueResponse(page: Int, callback: (Result<MovieResponse>) -> Unit) {
        enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    callback(Result.success(response.body() ?: MovieResponse(page, emptyList(), 0, 0)))
                } else {
                    callback(Result.failure(HttpException(response)))
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    fun searchMovies(query: String, callback: (Result<List<Movie>>) -> Unit) {
        if (query.isBlank()) {
            callback(Result.success(emptyList()))
            return
        }

        val key = runCatching { requireApiKey() }.getOrElse {
            callback(Result.failure(it))
            return
        }

        api.searchMovies(key, query).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    callback(Result.success(body?.results ?: emptyList()))
                } else {
                    callback(Result.failure(HttpException(response)))
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    // Backwards-compatible convenience methods
    fun getTopRated(callback: (Result<List<Movie>>) -> Unit) {
        getTopRatedPage { res -> callback(res.map { it.results }) }
    }

    fun getPopular(callback: (Result<List<Movie>>) -> Unit) {
        getPopularPage { res -> callback(res.map { it.results }) }
    }

    fun getNowPlaying(callback: (Result<List<Movie>>) -> Unit) {
        getNowPlayingPage { res -> callback(res.map { it.results }) }
    }

    // Paginated methods returning MovieResponse (page & total_pages)
    private fun getTopRatedPage(callback: (Result<MovieResponse>) -> Unit) {
        val key = runCatching { requireApiKey() }.getOrElse {
            callback(Result.failure(it))
            return
        }
        api.getTopRated(key, 1).enqueueResponse(1, callback)
    }

    private fun getPopularPage(callback: (Result<MovieResponse>) -> Unit) {
        val key = runCatching { requireApiKey() }.getOrElse {
            callback(Result.failure(it))
            return
        }
        api.getPopular(key, 1).enqueueResponse(1, callback)
    }

    private fun getNowPlayingPage(callback: (Result<MovieResponse>) -> Unit) {
        val key = runCatching { requireApiKey() }.getOrElse {
            callback(Result.failure(it))
            return
        }
        api.getNowPlaying(key, 1).enqueueResponse(1, callback)
    }

    // New suspend helpers for use by PagingSource
    private suspend fun fetchPage(call: Call<MovieResponse>, page: Int): MovieResponse {
        val response = call.awaitResponse()
        if (response.isSuccessful) {
            return response.body() ?: MovieResponse(page, emptyList(), 0, 0)
        }
        throw HttpException(response)
    }

    suspend fun getTopRatedPageSuspend(page: Int = 1): MovieResponse {
        val key = requireApiKey()
        return fetchPage(api.getTopRated(key, page), page)
    }

    suspend fun getPopularPageSuspend(page: Int = 1): MovieResponse {
        val key = requireApiKey()
        return fetchPage(api.getPopular(key, page), page)
    }

    suspend fun getNowPlayingPageSuspend(page: Int = 1): MovieResponse {
        val key = requireApiKey()
        return fetchPage(api.getNowPlaying(key, page), page)
    }

    // Interface implementations
    override suspend fun getTopRated(): Result<List<Movie>> = runCatching {
        getTopRatedPageSuspend().results
    }

    override suspend fun getPopular(): Result<List<Movie>> = runCatching {
        getPopularPageSuspend().results
    }

    override suspend fun getNowPlaying(): Result<List<Movie>> = runCatching {
        getNowPlayingPageSuspend().results
    }

    override suspend fun searchMovies(query: String): Result<List<Movie>> = suspendCancellableCoroutine { continuation ->
        searchMovies(query) { result ->
            continuation.resume(result)
        }
    }
}
