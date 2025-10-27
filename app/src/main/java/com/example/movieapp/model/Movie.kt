package com.example.movieapp.model

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int,
    val title: String?,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Float?,
    @SerializedName("genre_ids") val genreIds: List<Int>? = emptyList()
)
