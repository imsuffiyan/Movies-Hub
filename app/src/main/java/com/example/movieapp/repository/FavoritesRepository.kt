package com.example.movieapp.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.movieapp.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    private fun getMap(): MutableMap<String, Movie> {
        val json = prefs.getString("movies_map", null) ?: return mutableMapOf()
        val type = object : TypeToken<MutableMap<String, Movie>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }

    private fun saveMap(map: Map<String, Movie>) {
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

