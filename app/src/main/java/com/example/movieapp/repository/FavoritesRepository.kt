package com.example.movieapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.movieapp.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.jvm.Volatile

class FavoritesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    @Volatile
    private var cache: MutableMap<String, Movie>? = null

    private fun getMap(): MutableMap<String, Movie> {
        cache?.let { return it }
        val stored = prefs.getString("movies_map", null)
        val map = if (stored.isNullOrBlank()) {
            mutableMapOf()
        } else {
            val type = object : TypeToken<MutableMap<String, Movie>>() {}.type
            gson.fromJson<MutableMap<String, Movie>>(stored, type) ?: mutableMapOf()
        }
        cache = map
        return map
    }

    private fun saveMap(map: MutableMap<String, Movie>) {
        cache = map
        prefs.edit().putString("movies_map", gson.toJson(map)).apply()
    }

    fun add(movie: Movie) {
        val map = getMap()
        map[movie.id.toString()] = movie
        saveMap(map)
    }

    fun remove(movieId: Int) {
        val map = getMap()
        map.remove(movieId.toString())
        saveMap(map)
    }

    fun isFavorite(movieId: Int): Boolean {
        val map = getMap()
        return map.containsKey(movieId.toString())
    }

    fun allFavorites(): List<Movie> {
        val map = getMap()
        return map.values.toList()
    }
}
