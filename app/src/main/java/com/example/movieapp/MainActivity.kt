package com.example.movieapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.model.Movie
import com.example.movieapp.model.Section
import com.example.movieapp.repository.MovieRepository
import com.example.movieapp.ui.SectionAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repo: MovieRepository
    private lateinit var sectionAdapter: SectionAdapter
    private val sections = mutableListOf(
        Section(title = "Rated Movies", category = "top_rated"),
        Section(title = "Popular Movies", category = "popular"),
        Section(title = "Newest Movies", category = "now_playing")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        // shared view pool for inner recyclers
        val viewPool = RecyclerView.RecycledViewPool()

        // Section adapter with callbacks for See All and item clicks
        sectionAdapter = SectionAdapter(
            viewPool,
            onSeeAll = { category, title -> startCategory(category, title) },
            onItemClick = { movie -> openDetail(movie) }
        )

        val recyclerSections = findViewById<RecyclerView>(R.id.recycler_sections)
        recyclerSections.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerSections.adapter = sectionAdapter
        sectionAdapter.submitSections(sections)

        // Load data for sections
        loadCategories()
    }

    private fun loadCategories() {
        sections.forEach { loadSection(it.category) }
    }

    private fun loadSection(category: String) {
        val callback: (Result<List<Movie>>) -> Unit = { res ->
            res.fold(onSuccess = { movies ->
                runOnUiThread { updateSectionMovies(category, movies) }
            }, onFailure = { error ->
                runOnUiThread { showSectionError(category, error) }
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
        val root: View = findViewById(android.R.id.content)
        Snackbar.make(root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) { loadSection(category) }
            .show()
    }

    private fun startCategory(category: String, title: String) {
        val intent = Intent(this, CategoryListActivity::class.java)
        intent.putExtra(CategoryListActivity.EXTRA_CATEGORY, category)
        intent.putExtra(CategoryListActivity.EXTRA_TITLE, title)
        startActivity(intent)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
            startActivity(Intent(this, SearchActivity::class.java))
            return true
        }
        else if (item.itemId == R.id.action_fav) {
            startCategory("favorite", "Favorite Movies")
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}