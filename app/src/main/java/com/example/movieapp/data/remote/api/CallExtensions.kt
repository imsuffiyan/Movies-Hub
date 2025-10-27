package com.example.movieapp.data.remote.api

import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Call<T>.awaitResponse(): Response<T> = suspendCancellableCoroutine { cont ->
    enqueue(object : retrofit2.Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (!cont.isCancelled) cont.resume(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            if (!cont.isCancelled) cont.resumeWithException(t)
        }
    })
    cont.invokeOnCancellation { runCatching { cancel() } }
}
