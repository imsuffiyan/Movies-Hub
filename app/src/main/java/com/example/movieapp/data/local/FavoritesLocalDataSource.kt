package com.example.movieapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.movieapp.domain.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import kotlin.jvm.Volatile

class FavoritesLocalDataSource @Inject constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    @Volatile
    private var cache: MutableMap<String, Movie>? = null

    private fun getMap(): MutableMap<String, Movie> {
        cache?.let { return it }
        val stored = prefs.getString(KEY_MOVIES_MAP, null)
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
        prefs.edit().putString(KEY_MOVIES_MAP, gson.toJson(map)).apply()
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

    companion object {
        private const val PREFS_NAME = "favorites"
        private const val KEY_MOVIES_MAP = "movies_map"
    }
}
