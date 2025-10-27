package com.example.movieapp.domain.model

enum class MovieCategory(val value: String) {
    POPULAR("popular"),
    TOP_RATED("top_rated"),
    NOW_PLAYING("now_playing"),
    FAVORITE("favorite");

    companion object {
        fun fromValue(value: String): MovieCategory = entries.firstOrNull { it.value == value }
            ?: POPULAR
    }
}
