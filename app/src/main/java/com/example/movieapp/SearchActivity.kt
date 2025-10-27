package com.example.movieapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.model.Movie
import com.example.movieapp.repository.MovieRepository
import com.example.movieapp.ui.MovieAdapter
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    @Inject
    lateinit var repo: MovieRepository
    private lateinit var adapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val searchView = findViewById<SearchView>(R.id.search_view)
        val searchProgress = findViewById<CircularProgressIndicator>(R.id.search_progress)
        val recycler = findViewById<RecyclerView>(R.id.search_results)
        adapter = MovieAdapter(onItemClick = { movie -> openDetail(movie) })
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        searchView.isIconified = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val q = it.trim()
                    if (q.isNotEmpty()) {
                        searchProgress.visibility = View.VISIBLE
                        doSearch(q, searchProgress)
                    }
                }
                searchView.clearFocus()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }


    private fun doSearch(query: String, progress: CircularProgressIndicator) {
        progress.visibility = View.VISIBLE
        repo.searchMovies(query) { result ->
            result.onSuccess { movies ->
                runOnUiThread {
                    progress.visibility = View.GONE
                    adapter.update(movies)
                }
            }
            result.onFailure { error ->
                runOnUiThread {
                    progress.visibility = View.GONE
                    val message = error.message.takeUnless { it.isNullOrBlank() }
                        ?: getString(R.string.error_loading_search)
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry) { doSearch(query, progress) }
                        .show()
                }
            }
        }
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
}
