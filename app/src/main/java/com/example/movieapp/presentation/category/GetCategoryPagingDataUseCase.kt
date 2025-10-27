package com.example.movieapp.presentation.category

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.repository.MovieRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCategoryPagingDataUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(category: MovieCategory): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { CategoryPagingSource(repository, category) },
        ).flow
    }
}
