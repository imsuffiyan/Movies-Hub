package com.example.movieapp.ui.search

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
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
import com.example.movieapp.ui.MovieAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModels()

    private lateinit var adapter: MovieAdapter
    private var recycler: RecyclerView? = null
    private var progress: CircularProgressIndicator? = null
    private var emptyView: TextView? = null
    private var instructionView: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.search_toolbar)
        toolbar.navigationIcon = AppCompatResources.getDrawable(requireContext(), androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val searchView = view.findViewById<SearchView>(R.id.search_view)
        progress = view.findViewById(R.id.search_progress)
        recycler = view.findViewById(R.id.search_results)
        emptyView = view.findViewById(R.id.search_empty)
        instructionView = view.findViewById(R.id.search_instruction)

        adapter = MovieAdapter(onItemClick = { movie -> openDetail(movie) })
        recycler?.layoutManager = LinearLayoutManager(requireContext())
        recycler?.adapter = adapter

        searchView.isIconified = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val q = it.trim()
                    if (q.isNotEmpty()) {
                        viewModel.search(q)
                    }
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        collectState()
    }

    override fun onDestroyView() {
        recycler?.adapter = null
        recycler = null
        progress = null
        emptyView = null
        instructionView = null
        super.onDestroyView()
    }

    private fun openDetail(movie: Movie) {
        val action = SearchFragmentDirections.actionSearchFragmentToMovieDetailFragment(
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

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progress?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    instructionView?.visibility = if (state.showInstruction) View.VISIBLE else View.GONE
                    emptyView?.visibility = if (state.showEmptyState) View.VISIBLE else View.GONE
                    adapter.update(state.results)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SearchEvent.Error -> showError(event.message)
                    }
                }
            }
        }
    }

    private fun showError(message: String?) {
        val text = message.takeUnless { it.isNullOrBlank() } ?: getString(R.string.error_loading_search)
        view?.let { root ->
            Snackbar.make(root, text, Snackbar.LENGTH_LONG).show()
        }
    }
}
