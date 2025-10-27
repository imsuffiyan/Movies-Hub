package com.example.movieapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.movieapp.repository.FavoritesRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.example.movieapp.util.GenreUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale
import com.example.movieapp.model.Movie

class MovieDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_OVERVIEW = "extra_overview"
        const val EXTRA_POSTER = "extra_poster"
        const val EXTRA_RELEASE_DATE = "extra_release_date"
        const val EXTRA_VOTE = "extra_vote"
        const val EXTRA_GENRE_IDS = "extra_genre_ids"
        const val EXTRA_ID = "extra_id"
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_movie_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val ivPoster = findViewById<ImageView>(R.id.detail_poster)
        val tvTitle = findViewById<TextView>(R.id.detail_title)
        val tvRelease = findViewById<TextView>(R.id.detail_release)
        val tvOverview = findViewById<TextView>(R.id.detail_overview)
        val chipGroup = findViewById<ChipGroup>(R.id.detail_genres)
        val tvRating = findViewById<TextView>(R.id.detail_rating)
        val container = findViewById<View>(R.id.detail_content)
        val fab = findViewById<FloatingActionButton>(R.id.fab_favorite)

        val favRepo = FavoritesRepository(this)

        val title = intent.getStringExtra(EXTRA_TITLE)
        val overview = intent.getStringExtra(EXTRA_OVERVIEW)
        val poster = intent.getStringExtra(EXTRA_POSTER)
        val release = intent.getStringExtra(EXTRA_RELEASE_DATE)
        val vote = intent.getFloatExtra(EXTRA_VOTE, -1f)
        val genreIds = intent.getIntArrayExtra(EXTRA_GENRE_IDS)
        val movieId = intent.getIntExtra(EXTRA_ID, -1)

        tvTitle.text = title ?: getString(R.string.app_name)
        tvOverview.text = overview ?: ""
        // show only year if available
        val year = release?.takeIf { it.isNotBlank() }?.substringBefore('-')
        tvRelease.text = year ?: (release ?: "")
        if (vote >= 0f) {
            tvRating.text = String.format(Locale.getDefault(), "\u2605 %.1f", vote)
        } else {
            tvRating.text = ""
        }

        // genres into chips
        val genreNames = GenreUtils.namesForIds(genreIds?.toList())
        chipGroup.removeAllViews()
        val chipBg = resources.getColor(R.color.rating_overlay, theme)
        val chipText = resources.getColor(R.color.colorOnPrimary, theme)
        val bgState = android.content.res.ColorStateList.valueOf(chipBg)
        for (name in genreNames) {
            val chip = Chip(this)
            chip.text = name
            chip.isClickable = false
            chip.isCheckable = false
            chip.chipBackgroundColor = bgState
            chip.setTextColor(chipText)
            // use non-deprecated API to set corner radius
            chip.chipCornerRadius = resources.getDimension(R.dimen.chip_corner_radius)
            chipGroup.addView(chip)
        }

        // load poster (use placeholder if absent)
        val imageUrl = poster?.let { "https://image.tmdb.org/t/p/w780$it" }
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_launcher_background).into(ivPoster)
        } else {
            ivPoster.setImageResource(R.drawable.ic_launcher_background)
        }

        // Setup favorite FAB state and click handling (requires movie id)
        if (movieId > 0) {
            updateFabIcon(fab, favRepo.isFavorite(movieId))
            fab.setOnClickListener { v ->
                val isFav = favRepo.isFavorite(movieId)
                if (isFav) {
                    favRepo.remove(movieId)
                    updateFabIcon(fab, false)
                    Snackbar.make(v, getString(R.string.removed_from_favorites), Snackbar.LENGTH_SHORT).show()
                } else {
                    // reconstruct minimal Movie object to save
                    val movie = Movie(
                        id = movieId,
                        title = title,
                        overview = overview,
                        posterPath = poster,
                        releaseDate = release,
                        voteAverage = if (vote >= 0f) vote else null,
                        genreIds = genreIds?.toList()
                    )
                    favRepo.add(movie)
                    updateFabIcon(fab, true)
                    Snackbar.make(v, getString(R.string.added_to_favorites), Snackbar.LENGTH_SHORT).show()
                }
            }
        } else {
            // hide FAB if we don't have an id
            fab.isVisible = false
        }

        container.isVisible = true
    }

    private fun updateFabIcon(fab: FloatingActionButton, isFav: Boolean) {
        fab.setImageResource(if (isFav) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
