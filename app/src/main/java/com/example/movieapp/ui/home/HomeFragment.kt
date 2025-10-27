package com.example.movieapp.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.model.Movie
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

        setupToolbar(view)
        setupRecyclerView(view)
        observeViewModel()
    }

    override fun onDestroyView() {
        recyclerView?.adapter = null
        recyclerView = null
        super.onDestroyView()
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.main_toolbar)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSearchFragment())
                    true
                }
                R.id.action_fav -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToCategoryListFragment(
                        "favorite",
                        getString(R.string.favorite_movies)
                    )
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val viewPool = RecyclerView.RecycledViewPool()
        sectionAdapter = SectionAdapter(
            viewPool,
            onSeeAll = { category, title ->
                val action = HomeFragmentDirections.actionHomeFragmentToCategoryListFragment(category, title)
                findNavController().navigate(action)
            },
            onItemClick = { movie -> navigateToDetail(movie) }
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_sections).apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = sectionAdapter
            
            // Add spacing for better layout
            if (itemDecorationCount == 0) {
                addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(
                    requireContext(), 
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                ))
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }
    }

    private fun handleUiState(state: HomeUiState) {
        sectionAdapter.submitSections(state.sections)
        
        if (state.hasError) {
            showError(state.error!!)
        }
    }

    private fun showError(message: String) {
        view?.let { root ->
            Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry) { 
                    viewModel.clearError()
                    viewModel.loadAllSections() 
                }
                .show()
        }
    }

    private fun navigateToDetail(movie: Movie) {
        val action = HomeFragmentDirections.actionHomeFragmentToMovieDetailFragment(
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
