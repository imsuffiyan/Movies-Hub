package com.example.movieapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.repository.FavoritesRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : FavoritesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val mutex = Mutex()

    private val favoritesFlow = MutableStateFlow(loadFavorites())

    override fun observeFavorites(): Flow<List<Movie>> = favoritesFlow.asStateFlow()

    override suspend fun addFavorite(movie: Movie) {
        updateFavorites { map ->
            map[movie.id.toString()] = movie
        }
    }

    override suspend fun removeFavorite(movieId: Int) {
        updateFavorites { map ->
            map.remove(movieId.toString())
        }
    }

    override suspend fun isFavorite(movieId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                loadMap().containsKey(movieId.toString())
            }
        }
    }

    private suspend fun updateFavorites(block: (MutableMap<String, Movie>) -> Unit) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val map = loadMap()
                block(map)
                saveMap(map)
                favoritesFlow.value = map.values.toList()
            }
        }
    }

    private fun loadFavorites(): List<Movie> {
        return loadMap().values.toList()
    }

    private fun loadMap(): MutableMap<String, Movie> {
        val stored = prefs.getString("movies_map", null)
        if (stored.isNullOrBlank()) {
            return mutableMapOf()
        }
        val type = object : TypeToken<MutableMap<String, Movie>>() {}.type
        return gson.fromJson<MutableMap<String, Movie>>(stored, type) ?: mutableMapOf()
    }

    private fun saveMap(map: MutableMap<String, Movie>) {
        prefs.edit().putString("movies_map", gson.toJson(map)).apply()
    }
}
