package com.example.movieapp.ui.category

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.paging.CategoryLoadStateAdapter
import com.example.movieapp.paging.CategoryPagingAdapter
import com.example.movieapp.ui.MovieAdapter
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryListFragment : Fragment(R.layout.fragment_category_list) {

    private val viewModel: CategoryViewModel by viewModels()
    private val args: CategoryListFragmentArgs by navArgs()

    private lateinit var pagingAdapter: CategoryPagingAdapter
    private lateinit var loadStateAdapter: CategoryLoadStateAdapter
    private lateinit var legacyAdapter: MovieAdapter
    private var recycler: RecyclerView? = null

    private var pagingJob: Job? = null
    private var favoritesJob: Job? = null
    private var currentCategory: MovieCategory = MovieCategory.POPULAR

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pagingAdapter = CategoryPagingAdapter(onItemClick = { movie -> openDetail(movie) })
        loadStateAdapter = CategoryLoadStateAdapter { pagingAdapter.retry() }
        legacyAdapter = MovieAdapter(onItemClick = { movie -> openDetail(movie) })

        recycler = view.findViewById<RecyclerView>(R.id.category_list).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }

        val toolbar = view.findViewById<MaterialToolbar>(R.id.category_toolbar)
        toolbar.title = args.title
        toolbar.navigationIcon = AppCompatResources.getDrawable(requireContext(), androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        currentCategory = MovieCategory.fromId(args.category)
        displayCategory(currentCategory)
    }

    override fun onResume() {
        super.onResume()
        if (currentCategory == MovieCategory.FAVORITE) {
            refreshFavorites()
        }
    }

    override fun onDestroyView() {
        pagingJob?.cancel()
        favoritesJob?.cancel()
        recycler?.adapter = null
        recycler = null
        super.onDestroyView()
    }

    private fun displayCategory(category: MovieCategory) {
        currentCategory = category
        pagingJob?.cancel()
        favoritesJob?.cancel()

        if (category == MovieCategory.FAVORITE) {
            recycler?.adapter = legacyAdapter
            refreshFavorites()
            return
        }

        recycler?.adapter = pagingAdapter.withLoadStateFooter(loadStateAdapter)
        pagingJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.moviesFor(category).collectLatest { pagingData ->
                    pagingAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun refreshFavorites() {
        favoritesJob?.cancel()
        favoritesJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { favorites ->
                    legacyAdapter.update(favorites)
                }
            }
        }
    }

    private fun openDetail(movie: Movie) {
        val action = CategoryListFragmentDirections.actionCategoryListFragmentToMovieDetailFragment(
            movie.title,
            movie.overview,
            movie.posterPath,
            movie.releaseDate,
            movie.voteAverage ?: -1f,
            movie.genreIds.takeIf { it.isNotEmpty() }?.toIntArray(),
            movie.id
        )
        findNavController().navigate(action)
    }
}
