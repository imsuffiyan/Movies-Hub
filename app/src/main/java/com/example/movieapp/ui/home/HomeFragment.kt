package com.example.movieapp.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.ui.SectionAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var sectionAdapter: SectionAdapter
    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.main_toolbar)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSearchFragment())
                    true
                }
                R.id.action_fav -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToCategoryListFragment(
                        MovieCategory.FAVORITE.id,
                        getString(R.string.favorite_movies)
                    )
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }

        val viewPool = RecyclerView.RecycledViewPool()
        sectionAdapter = SectionAdapter(
            viewPool,
            onSeeAll = { section -> navigateToCategory(section) },
            onItemClick = { movie -> navigateToDetail(movie) }
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_sections).apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = sectionAdapter
        }

        collectState()
    }

    override fun onDestroyView() {
        recyclerView?.adapter = null
        recyclerView = null
        super.onDestroyView()
    }

    private fun navigateToDetail(movie: Movie) {
        val action = HomeFragmentDirections.actionHomeFragmentToMovieDetailFragment(
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

    private fun navigateToCategory(section: SectionUiModel) {
        val action = HomeFragmentDirections.actionHomeFragmentToCategoryListFragment(
            section.category.id,
            section.title,
        )
        findNavController().navigate(action)
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val sections = buildSections(state)
                    sectionAdapter.submitSections(sections)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is HomeEvent.SectionError -> showSectionError(event)
                    }
                }
            }
        }
    }

    private fun buildSections(state: HomeUiState): List<SectionUiModel> {
        val base = listOf(
            MovieCategory.TOP_RATED,
            MovieCategory.POPULAR,
            MovieCategory.NOW_PLAYING,
        )
        return base.map { category ->
            val sectionState = state.sections[category] ?: SectionState()
            SectionUiModel(
                title = when (category) {
                    MovieCategory.TOP_RATED -> getString(R.string.rated_movies)
                    MovieCategory.NOW_PLAYING -> getString(R.string.newest_movies)
                    else -> getString(R.string.popular_movies)
                },
                category = category,
                movies = sectionState.movies,
                isLoading = sectionState.isLoading,
                errorMessage = sectionState.errorMessage,
            )
        }
    }

    private fun showSectionError(event: HomeEvent.SectionError) {
        val title = when (event.category) {
            MovieCategory.TOP_RATED -> getString(R.string.rated_movies)
            MovieCategory.NOW_PLAYING -> getString(R.string.newest_movies)
            else -> getString(R.string.popular_movies)
        }
        val message = event.message.takeIf { it.isNotBlank() }
            ?: getString(R.string.error_loading_section, title)
        view?.let { root ->
            Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry) { viewModel.refreshSections() }
                .show()
        }
    }
}
