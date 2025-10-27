package com.example.movieapp.network

import retrofit2.Call
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun <T> Call<T>.awaitResponse(): Response<T> = suspendCancellableCoroutine { cont ->
    enqueue(object : retrofit2.Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (!cont.isCancelled) cont.resume(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            if (!cont.isCancelled) cont.resumeWithException(t)
        }
    })
    cont.invokeOnCancellation { try { cancel() } catch (_: Throwable) {} }
}

