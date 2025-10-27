package com.example.movieapp.ui.search

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.model.Movie
import com.example.movieapp.repository.MovieRepository
import com.example.movieapp.ui.MovieAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    @Inject
    lateinit var repo: MovieRepository

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
                        instructionView?.visibility = View.GONE
                        performSearch(q)
                    }
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    override fun onDestroyView() {
        recycler?.adapter = null
        recycler = null
        progress = null
        emptyView = null
        instructionView = null
        super.onDestroyView()
    }

    private fun performSearch(query: String) {
        progress?.visibility = View.VISIBLE
        emptyView?.visibility = View.GONE
        repo.searchMovies(query) { result ->
            result.onSuccess { movies ->
                view?.post {
                    progress?.visibility = View.GONE
                    adapter.update(movies)
                    emptyView?.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            result.onFailure { error ->
                view?.post {
                    progress?.visibility = View.GONE
                    val message = error.message.takeUnless { it.isNullOrBlank() }
                        ?: getString(R.string.error_loading_search)
                    view?.let { root ->
                        Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry) { performSearch(query) }
                            .show()
                    }
                }
            }
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
