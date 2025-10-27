package com.example.movieapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.usecase.GetCategoryMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCategoryMoviesUseCase: GetCategoryMoviesUseCase,
) : ViewModel() {

    private val _sections = MutableStateFlow(DEFAULT_SECTIONS)
    val sections: StateFlow<List<SectionUiModel>> = _sections.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        MovieCategory.entries
            .filter { it != MovieCategory.FAVORITE }
            .forEach { loadSection(it) }
    }

    fun refreshCategory(category: MovieCategory) {
        if (category == MovieCategory.FAVORITE) return
        loadSection(category)
    }

    private fun loadSection(category: MovieCategory) {
        _sections.update { sections ->
            sections.map { section ->
                if (section.category == category) {
                    section.copy(isLoading = true, errorMessage = null)
                } else {
                    section
                }
            }
        }

        viewModelScope.launch {
            val result = getCategoryMoviesUseCase(category)
            _sections.update { sections ->
                sections.map { section ->
                    if (section.category == category) {
                        result.fold(
                            onSuccess = { movies ->
                                section.copy(movies = movies, isLoading = false, errorMessage = null)
                            },
                            onFailure = { error ->
                                section.copy(
                                    isLoading = false,
                                    errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE,
                                )
                            },
                        )
                    } else {
                        section
                    }
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_ERROR_MESSAGE = "Unable to load movies"
        private val DEFAULT_SECTIONS = listOf(
            SectionUiModel(title = "Rated Movies", category = MovieCategory.TOP_RATED, isLoading = true),
            SectionUiModel(title = "Popular Movies", category = MovieCategory.POPULAR, isLoading = true),
            SectionUiModel(title = "Newest Movies", category = MovieCategory.NOW_PLAYING, isLoading = true),
        )
    }
}
