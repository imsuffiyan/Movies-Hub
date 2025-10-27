package com.example.movieapp.ui.home

import com.example.movieapp.model.Section

data class HomeUiState(
    val sections: List<Section> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = sections.isEmpty() && !isLoading
}