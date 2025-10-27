package com.example.movieapp.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.usecase.GetFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategoryPagingDataUseCase: GetCategoryPagingDataUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
) : ViewModel() {

    private val _favoriteMovies = MutableStateFlow<List<Movie>>(emptyList())
    val favoriteMovies: StateFlow<List<Movie>> = _favoriteMovies.asStateFlow()

    fun moviesFor(category: MovieCategory): Flow<PagingData<Movie>> {
        return getCategoryPagingDataUseCase(category).cachedIn(viewModelScope)
    }

    fun loadFavorites() {
        viewModelScope.launch {
            val result = getFavoritesUseCase()
            result.onSuccess { movies ->
                _favoriteMovies.value = movies
            }.onFailure {
                _favoriteMovies.value = emptyList()
            }
        }
    }
}
