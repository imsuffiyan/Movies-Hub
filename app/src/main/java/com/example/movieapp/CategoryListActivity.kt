package com.example.movieapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
class CategoryListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_TITLE = "extra_title"
    }

    // ViewModel injected by Hilt
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var favRepo: FavoritesRepository

    // UI adapters
    private lateinit var pagingAdapter: CategoryPagingAdapter
    private lateinit var loadStateAdapter: CategoryLoadStateAdapter
    private lateinit var legacyAdapter: MovieAdapter
    private lateinit var recycler: RecyclerView

    // Paging collection job so we can cancel when switching categories
    private var pagingJob: Job? = null

    // currently displayed category (used for favorites fallback)
    private var currentCategory: String = "popular"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.category_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        favRepo = FavoritesRepository(this)

        // adapters
        pagingAdapter = CategoryPagingAdapter(onItemClick = { movie -> openDetail(movie) })
        loadStateAdapter = CategoryLoadStateAdapter { pagingAdapter.retry() }

        legacyAdapter = MovieAdapter(onItemClick = { movie -> openDetail(movie) })

        recycler = findViewById(R.id.category_list)
        val layoutManager = LinearLayoutManager(this)
        recycler.layoutManager = layoutManager

        // read incoming category and title
        val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)
        supportActionBar?.title = title

        currentCategory = intent.getStringExtra(EXTRA_CATEGORY) ?: "popular"

        // show the appropriate adapter & start streaming data
        displayCategory(currentCategory)
    }

    override fun onResume() {
        super.onResume()
        if (currentCategory == "favorite") {
            refreshFavorites()
        }
    }

    private fun displayCategory(category: String) {
        currentCategory = category
        // cancel any existing paging collector
        pagingJob?.cancel()
        // favorites (either explicit favorites category, or for popular if local favorites exist)
        if (category == "favorite") {
            refreshFavorites()
            return
        }

        // Use Paging for network-backed categories
        recycler.adapter = pagingAdapter.withLoadStateFooter(loadStateAdapter)

        // collect paging data and submit to adapter; cancel previous collector first
        pagingJob = lifecycleScope.launch {
            viewModel.moviesFor(category).collectLatest { pagingData ->
                pagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun refreshFavorites() {
        recycler.adapter = legacyAdapter
        val favs = favRepo.allFavorites()
        legacyAdapter.update(favs)
    }

    private fun openDetail(movie: Movie) {
        val intent = Intent(this, MovieDetailActivity::class.java)
        intent.putExtra(MovieDetailActivity.EXTRA_TITLE, movie.title)
        intent.putExtra(MovieDetailActivity.EXTRA_OVERVIEW, movie.overview)
        intent.putExtra(MovieDetailActivity.EXTRA_POSTER, movie.posterPath)
        intent.putExtra(MovieDetailActivity.EXTRA_RELEASE_DATE, movie.releaseDate)
        intent.putExtra(MovieDetailActivity.EXTRA_VOTE, movie.voteAverage ?: -1f)
        intent.putExtra(MovieDetailActivity.EXTRA_GENRE_IDS, movie.genreIds?.toIntArray())
        intent.putExtra(MovieDetailActivity.EXTRA_ID, movie.id)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
