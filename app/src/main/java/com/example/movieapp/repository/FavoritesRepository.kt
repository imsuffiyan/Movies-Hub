package com.example.movieapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.movieapp.domain.repository.FavoritesRepositoryInterface
import com.example.movieapp.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.Volatile

@Singleton
class FavoritesRepository @Inject constructor(
    @ApplicationContext context: Context
) : FavoritesRepositoryInterface {
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

    override fun addToFavorites(movie: Movie) {
        val map = getMap()
        map[movie.id.toString()] = movie
        saveMap(map)
    }

    override fun removeFromFavorites(movieId: Int) {
        val map = getMap()
        map.remove(movieId.toString())
        saveMap(map)
    }

    override fun isFavorite(movieId: Int): Boolean {
        val map = getMap()
        return map.containsKey(movieId.toString())
    }

    override fun getAllFavorites(): List<Movie> {
        val map = getMap()
        return map.values.toList()
    }

    // Legacy methods for backward compatibility
    fun add(movie: Movie) = addToFavorites(movie)
    fun remove(movieId: Int) = removeFromFavorites(movieId)
    fun allFavorites(): List<Movie> = getAllFavorites()
}
