package com.example.movieapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.domain.usecase.GetMoviesPageUseCase

class CategoryPagingSource(
    private val category: MovieCategory,
    private val getMoviesPageUseCase: GetMoviesPageUseCase,
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        val result = getMoviesPageUseCase(category, page)
        return result.fold(
            onSuccess = { response ->
                val prevKey = if (response.page > 1) response.page - 1 else null
                val nextKey = if (response.page < response.totalPages) response.page + 1 else null
                LoadResult.Page(response.movies, prevKey, nextKey)
            },
            onFailure = { throwable -> LoadResult.Error(throwable) },
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPos ->
            val anchorPage = state.closestPageToPosition(anchorPos)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
