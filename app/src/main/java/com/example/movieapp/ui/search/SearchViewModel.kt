package com.example.movieapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieapp.domain.usecase.SearchMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMoviesUseCase: SearchMoviesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SearchEvent>()
    val events: SharedFlow<SearchEvent> = _events.asSharedFlow()

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState(showInstruction = true)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showInstruction = false, showEmptyState = false) }
            val result = searchMoviesUseCase(query)
            result.onSuccess { movies ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = movies,
                        showEmptyState = movies.isEmpty(),
                    )
                }
            }
            result.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false, showEmptyState = false) }
                _events.emit(SearchEvent.Error(throwable.message))
            }
        }
    }
}
