package com.example.movieapp.domain.model

enum class MovieCategory(val id: String) {
    POPULAR("popular"),
    TOP_RATED("top_rated"),
    NOW_PLAYING("now_playing"),
    FAVORITE("favorite");

    companion object {
        fun fromId(id: String): MovieCategory = values().firstOrNull { it.id == id }
            ?: POPULAR
    }
}
