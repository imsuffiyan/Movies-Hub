package com.example.movieapp.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.model.Movie
import com.example.movieapp.model.Section
import com.example.movieapp.repository.MovieRepository
import com.example.movieapp.ui.SectionAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    @Inject
    lateinit var repo: MovieRepository

    private lateinit var sectionAdapter: SectionAdapter
    private var recyclerView: RecyclerView? = null

    private val sections = mutableListOf(
        Section(title = "Rated Movies", category = "top_rated"),
        Section(title = "Popular Movies", category = "popular"),
        Section(title = "Newest Movies", category = "now_playing")
    )

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
                        "favorite",
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
            onSeeAll = { category, title ->
                val action = HomeFragmentDirections.actionHomeFragmentToCategoryListFragment(category, title)
                findNavController().navigate(action)
            },
            onItemClick = { movie -> navigateToDetail(movie) }
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_sections).apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = sectionAdapter
        }
        sectionAdapter.submitSections(sections)

        loadCategories()
    }

    override fun onDestroyView() {
        recyclerView?.adapter = null
        recyclerView = null
        super.onDestroyView()
    }

    private fun loadCategories() {
        sections.forEach { loadSection(it.category) }
    }

    private fun loadSection(category: String) {
        val callback: (Result<List<Movie>>) -> Unit = { res ->
            res.fold(onSuccess = { movies ->
                view?.post { updateSectionMovies(category, movies) }
            }, onFailure = { error ->
                view?.post { showSectionError(category, error) }
            })
        }

        when (category) {
            "top_rated" -> repo.getTopRated(callback)
            "popular" -> repo.getPopular(callback)
            "now_playing" -> repo.getNowPlaying(callback)
        }
    }

    private fun updateSectionMovies(category: String, movies: List<Movie>) {
        val idx = sections.indexOfFirst { it.category == category }
        if (idx >= 0) {
            sections[idx] = sections[idx].copy(movies = movies)
            sectionAdapter.submitSections(sections)
        }
    }

    private fun showSectionError(category: String, throwable: Throwable) {
        val title = sections.firstOrNull { it.category == category }?.title ?: category
        val message = throwable.message.takeUnless { it.isNullOrBlank() }
            ?: getString(R.string.error_loading_section, title)
        view?.let { root ->
            Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry) { loadSection(category) }
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
