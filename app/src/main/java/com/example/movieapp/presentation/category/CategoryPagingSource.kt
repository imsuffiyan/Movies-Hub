package com.example.movieapp.presentation.category

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.repository.MovieRepository

class CategoryPagingSource(
    private val repository: MovieRepository,
    private val category: MovieCategory,
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return try {
            val moviePage = repository.fetchCategoryPage(category, page)
            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (page < moviePage.totalPages) page + 1 else null
            LoadResult.Page(moviePage.movies, prevKey, nextKey)
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPos ->
            val anchorPage = state.closestPageToPosition(anchorPos)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
