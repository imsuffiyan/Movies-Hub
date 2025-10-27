package com.example.movieapp.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.usecase.GetMoviesPageUseCase
import com.example.movieapp.domain.usecase.ObserveFavoritesUseCase
import com.example.movieapp.paging.CategoryPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getMoviesPageUseCase: GetMoviesPageUseCase,
    observeFavoritesUseCase: ObserveFavoritesUseCase,
) : ViewModel() {

    val favorites: StateFlow<List<Movie>> = observeFavoritesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun moviesFor(category: MovieCategory): Flow<PagingData<Movie>> {
        val pager = Pager(
            config = PagingConfig(pageSize = 1, enablePlaceholders = false),
            pagingSourceFactory = { CategoryPagingSource(category, getMoviesPageUseCase) },
        )
        return pager.flow.cachedIn(viewModelScope)
    }
}
