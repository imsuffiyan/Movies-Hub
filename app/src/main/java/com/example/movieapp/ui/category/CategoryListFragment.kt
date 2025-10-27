package com.example.movieapp.ui.category

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.core.ui.createResponsiveGridLayoutManager
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.presentation.category.CategoryLoadStateAdapter
import com.example.movieapp.presentation.category.CategoryPagingAdapter
import com.example.movieapp.presentation.category.CategoryViewModel
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
    private lateinit var favoritesAdapter: MovieAdapter
    private var recycler: RecyclerView? = null

    private var pagingJob: Job? = null
    private var favoritesJob: Job? = null
    private var currentCategory: MovieCategory = MovieCategory.POPULAR

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pagingAdapter = CategoryPagingAdapter(onItemClick = { movie -> openDetail(movie) })
        loadStateAdapter = CategoryLoadStateAdapter { pagingAdapter.retry() }
        favoritesAdapter = MovieAdapter(onItemClick = { movie -> openDetail(movie) })

        recycler = view.findViewById<RecyclerView>(R.id.category_list).apply {
            itemAnimator = null
            clipToPadding = false
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(bottom = bars.bottom)
                insets
            }
        }

        val toolbar = view.findViewById<MaterialToolbar>(R.id.category_toolbar)
        toolbar.title = args.title
        toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireContext(),
            androidx.appcompat.R.drawable.abc_ic_ab_back_material,
        )
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        currentCategory = MovieCategory.fromValue(args.category)
        displayCategory(currentCategory)
    }

    override fun onResume() {
        super.onResume()
        if (currentCategory == MovieCategory.FAVORITE) {
            viewModel.loadFavorites()
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

        val layoutManager = createResponsiveGridLayoutManager(requireContext(), MIN_ITEM_WIDTH_DP)
        recycler?.layoutManager = layoutManager

        if (category == MovieCategory.FAVORITE) {
            recycler?.adapter = favoritesAdapter
            favoritesJob = viewLifecycleOwner.lifecycleScope.launch {
                viewModel.favoriteMovies.collectLatest { favoritesAdapter.update(it) }
            }
            viewModel.loadFavorites()
            return
        }

        recycler?.adapter = pagingAdapter.withLoadStateFooter(loadStateAdapter)
        if (layoutManager is GridLayoutManager) {
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val adapter = recycler?.adapter
                    if (adapter is ConcatAdapter) {
                        val mainCount = pagingAdapter.itemCount
                        return if (position < mainCount) 1 else layoutManager.spanCount
                    }
                    return 1
                }
            }
        }

        pagingJob = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moviesFor(category).collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
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
            movie.genreIds.toIntArray(),
            movie.id,
        )
        findNavController().navigate(action)
    }

    companion object {
        private const val MIN_ITEM_WIDTH_DP = 180
    }
}
