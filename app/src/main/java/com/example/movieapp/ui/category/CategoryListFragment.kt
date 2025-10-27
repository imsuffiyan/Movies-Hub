package com.example.movieapp.ui.category

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.model.Movie
import com.example.movieapp.paging.CategoryLoadStateAdapter
import com.example.movieapp.paging.CategoryPagingAdapter
import com.example.movieapp.paging.CategoryViewModel
import com.example.movieapp.ui.MovieAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryListFragment : Fragment(R.layout.fragment_category_list) {

    private val pagingViewModel: CategoryViewModel by viewModels()
    private val categoryViewModel: CategoryListViewModel by viewModels()
    private val args: CategoryListFragmentArgs by navArgs()

    private lateinit var pagingAdapter: CategoryPagingAdapter
    private lateinit var loadStateAdapter: CategoryLoadStateAdapter
    private lateinit var legacyAdapter: MovieAdapter
    private var recycler: RecyclerView? = null

    private var pagingJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        setupRecyclerView(view)
        setupViewModel()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        categoryViewModel.refreshFavorites()
    }

    override fun onDestroyView() {
        pagingJob?.cancel()
        recycler?.adapter = null
        recycler = null
        super.onDestroyView()
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.category_toolbar)
        toolbar.title = args.title
        toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireContext(), 
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView(view: View) {
        pagingAdapter = CategoryPagingAdapter(onItemClick = { movie -> openDetail(movie) })
        loadStateAdapter = CategoryLoadStateAdapter { pagingAdapter.retry() }
        legacyAdapter = MovieAdapter(onItemClick = { movie -> openDetail(movie) })

        recycler = view.findViewById<RecyclerView>(R.id.category_list).apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupViewModel() {
        categoryViewModel.initialize(args.category, args.title)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            categoryViewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }
    }

    private fun handleUiState(state: CategoryUiState) {
        when (state.category) {
            "favorite" -> {
                recycler?.adapter = legacyAdapter
                legacyAdapter.update(state.movies)
            }
            else -> {
                setupPagingAdapter(state.category)
            }
        }

        if (state.hasError) {
            showError(state.error!!)
        }
    }

    private fun setupPagingAdapter(category: String) {
        recycler?.adapter = pagingAdapter.withLoadStateFooter(loadStateAdapter)
        pagingJob?.cancel()
        pagingJob = viewLifecycleOwner.lifecycleScope.launch {
            pagingViewModel.moviesFor(category).collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun showError(message: String) {
        view?.let { root ->
            Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry) { 
                    categoryViewModel.clearError()
                    categoryViewModel.loadMovies()
                }
                .show()
        }
    }

    private fun openDetail(movie: Movie) {
        val action = CategoryListFragmentDirections.actionCategoryListFragmentToMovieDetailFragment(
            movie.title,
            movie.overview,
            movie.posterPath,
            movie.releaseDate,
            movie.voteAverage ?: -1f,
             movie.genreIds?.toIntArray(),
            movie.id
        )
        findNavController().navigate(action)
    }
}
