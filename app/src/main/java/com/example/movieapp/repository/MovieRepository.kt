package com.example.movieapp.repository

import com.example.movieapp.BuildConfig
import com.example.movieapp.model.Movie
import com.example.movieapp.model.MovieResponse
import com.example.movieapp.network.TmdbApi
import com.example.movieapp.network.awaitResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class MovieRepository(private val api: TmdbApi) {

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
        getTopRatedPage(1) { res -> callback(res.map { it.results }) }
    }

    fun getPopular(callback: (Result<List<Movie>>) -> Unit) {
        getPopularPage(1) { res -> callback(res.map { it.results }) }
    }

    fun getNowPlaying(callback: (Result<List<Movie>>) -> Unit) {
        getNowPlayingPage(1) { res -> callback(res.map { it.results }) }
    }

    // Paginated methods returning MovieResponse (page & total_pages)
    private fun getTopRatedPage(page: Int = 1, callback: (Result<MovieResponse>) -> Unit) {
        val key = runCatching { requireApiKey() }.getOrElse {
            callback(Result.failure(it))
            return
        }
        api.getTopRated(key, page).enqueueResponse(page, callback)
    }

    private fun getPopularPage(page: Int = 1, callback: (Result<MovieResponse>) -> Unit) {
        val key = runCatching { requireApiKey() }.getOrElse {
            callback(Result.failure(it))
            return
        }
        api.getPopular(key, page).enqueueResponse(page, callback)
    }

    private fun getNowPlayingPage(page: Int = 1, callback: (Result<MovieResponse>) -> Unit) {
        val key = runCatching { requireApiKey() }.getOrElse {
            callback(Result.failure(it))
            return
        }
        api.getNowPlaying(key, page).enqueueResponse(page, callback)
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
}
