package com.example.movieapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.movieapp.model.Movie
import com.example.movieapp.model.MovieResponse
import com.example.movieapp.repository.MovieRepository

class CategoryPagingSource(
    private val repository: MovieRepository,
    private val category: String
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return try {
            val body: MovieResponse = when (category) {
                "top_rated" -> repository.getTopRatedPageSuspend(page)
                "now_playing" -> repository.getNowPlayingPageSuspend(page)
                else -> repository.getPopularPageSuspend(page)
            }

            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (page < body.total_pages) page + 1 else null
            LoadResult.Page(body.results, prevKey, nextKey)
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
