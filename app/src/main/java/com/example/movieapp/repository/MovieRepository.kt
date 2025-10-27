package com.example.movieapp.repository

import com.example.movieapp.model.Movie
import com.example.movieapp.model.MovieResponse
import com.example.movieapp.network.TmdbApi
import com.example.movieapp.utils.Constant.TMDB_API_KEY
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class MovieRepository(private val api: TmdbApi) {

    fun searchMovies(query: String, callback: (Result<List<Movie>>) -> Unit) {
        if (query.isBlank()) {
            callback(Result.success(emptyList()))
            return
        }

        api.searchMovies(TMDB_API_KEY, query).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    callback(Result.success(body?.results ?: emptyList()))
                } else {
                    callback(Result.failure(Exception("API error: ${response.code()}")))
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    // Backwards-compatible convenience methods
    fun getTopRated(callback: (Result<List<Movie>>) -> Unit) {
        getTopRatedPage(1) { res ->
            res.fold(onSuccess = { callback(Result.success(it.results)) }, onFailure = { callback(Result.failure(it)) })
        }
    }

    fun getPopular(callback: (Result<List<Movie>>) -> Unit) {
        getPopularPage(1) { res ->
            res.fold(onSuccess = { callback(Result.success(it.results)) }, onFailure = { callback(Result.failure(it)) })
        }
    }

    fun getNowPlaying(callback: (Result<List<Movie>>) -> Unit) {
        getNowPlayingPage(1) { res ->
            res.fold(onSuccess = { callback(Result.success(it.results)) }, onFailure = { callback(Result.failure(it)) })
        }
    }

    // Paginated methods returning MovieResponse (page & total_pages)
    private fun getTopRatedPage(page: Int = 1, callback: (Result<MovieResponse>) -> Unit) {
        api.getTopRated(TMDB_API_KEY, page).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) callback(Result.success(response.body() ?: MovieResponse(page, emptyList(), 0, 0)))
                else callback(Result.failure(Exception("API error: ${response.code()}")))
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    private fun getPopularPage(page: Int = 1, callback: (Result<MovieResponse>) -> Unit) {
        api.getPopular(TMDB_API_KEY, page).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) callback(Result.success(response.body() ?: MovieResponse(page, emptyList(), 0, 0)))
                else callback(Result.failure(Exception("API error: ${response.code()}")))
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    private fun getNowPlayingPage(page: Int = 1, callback: (Result<MovieResponse>) -> Unit) {
        api.getNowPlaying(TMDB_API_KEY, page).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) callback(Result.success(response.body() ?: MovieResponse(page, emptyList(), 0, 0)))
                else callback(Result.failure(Exception("API error: ${response.code()}")))
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    // New suspend helpers for use by PagingSource
    suspend fun getTopRatedPageSuspend(page: Int = 1): MovieResponse = suspendCancellableCoroutine { cont ->
        val call = api.getTopRated(TMDB_API_KEY, page)
        call.enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (!cont.isCancelled) {
                    if (response.isSuccessful) cont.resume(response.body() ?: MovieResponse(page, emptyList(), 0, 0))
                    else cont.resumeWithException(Exception("API error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                if (!cont.isCancelled) cont.resumeWithException(t)
            }
        })
        cont.invokeOnCancellation { call.cancel() }
    }

    suspend fun getPopularPageSuspend(page: Int = 1): MovieResponse = suspendCancellableCoroutine { cont ->
        val call = api.getPopular(TMDB_API_KEY, page)
        call.enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (!cont.isCancelled) {
                    if (response.isSuccessful) cont.resume(response.body() ?: MovieResponse(page, emptyList(), 0, 0))
                    else cont.resumeWithException(Exception("API error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                if (!cont.isCancelled) cont.resumeWithException(t)
            }
        })
        cont.invokeOnCancellation { call.cancel() }
    }

    suspend fun getNowPlayingPageSuspend(page: Int = 1): MovieResponse = suspendCancellableCoroutine { cont ->
        val call = api.getNowPlaying(TMDB_API_KEY, page)
        call.enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (!cont.isCancelled) {
                    if (response.isSuccessful) cont.resume(response.body() ?: MovieResponse(page, emptyList(), 0, 0))
                    else cont.resumeWithException(Exception("API error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                if (!cont.isCancelled) cont.resumeWithException(t)
            }
        })
        cont.invokeOnCancellation { call.cancel() }
    }
}
