package com.example.movieapp.ui.category

import android.os.Bundle
import android.view.View
import androidx.core.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.example.movieapp.repository.FavoritesRepository
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

    private lateinit var favRepo: FavoritesRepository
    private lateinit var pagingAdapter: CategoryPagingAdapter
    private lateinit var loadStateAdapter: CategoryLoadStateAdapter
    private lateinit var legacyAdapter: MovieAdapter
    private var recycler: RecyclerView? = null

    private var pagingJob: Job? = null
    private var currentCategory: String = "popular"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        favRepo = FavoritesRepository(requireContext())

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

        currentCategory = args.category
        displayCategory(currentCategory)
    }

    override fun onResume() {
        super.onResume()
        if (currentCategory == "favorite") {
            refreshFavorites()
        }
    }

    override fun onDestroyView() {
        pagingJob?.cancel()
        recycler?.adapter = null
        recycler = null
        super.onDestroyView()
    }

    private fun displayCategory(category: String) {
        currentCategory = category
        pagingJob?.cancel()

        if (category == "favorite") {
            recycler?.adapter = legacyAdapter
            refreshFavorites()
            return
        }

        recycler?.adapter = pagingAdapter.withLoadStateFooter(loadStateAdapter)
        pagingJob = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moviesFor(category).collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun refreshFavorites() {
        val favorites = favRepo.allFavorites()
        legacyAdapter.update(favorites)
    }

    private fun openDetail(movie: Movie) {
        val action = CategoryListFragmentDirections.actionCategoryListFragmentToMovieDetailFragment(
            title = movie.title,
            overview = movie.overview,
            poster = movie.posterPath,
            releaseDate = movie.releaseDate,
            vote = movie.voteAverage ?: -1f,
            genreIds = movie.genreIds?.toIntArray(),
            id = movie.id
        )
        findNavController().navigate(action)
    }
}
