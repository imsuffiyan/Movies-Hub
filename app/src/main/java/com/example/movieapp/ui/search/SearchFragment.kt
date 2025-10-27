package com.example.movieapp.ui.search

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.model.Movie
import com.example.movieapp.ui.MovieAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var movieAdapter: MovieAdapter
    private var recyclerView: RecyclerView? = null
    private var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        setupSearchView(view)
        setupRecyclerView(view)
        observeViewModel()
    }

    override fun onDestroyView() {
        recyclerView?.adapter = null
        recyclerView = null
        searchView = null
        super.onDestroyView()
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.search_toolbar)
        toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireContext(),
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupSearchView(view: View) {
        searchView = view.findViewById<SearchView>(R.id.search_view).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.searchMovies(it) }
                    clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let { viewModel.searchMovies(it) }
                    return true
                }
            })
            requestFocus()
        }
    }

    private fun setupRecyclerView(view: View) {
        movieAdapter = MovieAdapter(onItemClick = { movie -> openDetail(movie) })

        recyclerView = view.findViewById<RecyclerView>(R.id.search_results).apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = movieAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }
    }

    private fun handleUiState(state: SearchUiState) {
        movieAdapter.update(state.movies)

        if (state.hasError) {
            showError(state.error!!)
        }

        // Handle empty states
        view?.findViewById<View>(R.id.search_empty)?.visibility =
            if (state.isEmpty) View.VISIBLE else View.GONE

        view?.findViewById<CircularProgressIndicator>(R.id.search_progress)?.visibility =
            if (state.isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        view?.let { root ->
            Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry) {
                    viewModel.clearError()
                    viewModel.retrySearch()
                }
                .show()
        }
    }

    private fun openDetail(movie: Movie) {
        val action = SearchFragmentDirections.actionSearchFragmentToMovieDetailFragment(
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
