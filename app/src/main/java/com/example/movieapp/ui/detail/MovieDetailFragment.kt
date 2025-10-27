package com.example.movieapp.ui.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.model.Movie
import com.example.movieapp.repository.FavoritesRepository
import com.example.movieapp.util.GenreUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MovieDetailFragment : Fragment(R.layout.fragment_movie_detail) {

    private val args: MovieDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = view.findViewById<MaterialToolbar>(R.id.detail_toolbar)
        toolbar.title = args.title ?: getString(R.string.app_name)
        toolbar.navigationIcon = AppCompatResources.getDrawable(requireContext(), androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val ivPoster = view.findViewById<ImageView>(R.id.detail_poster)
        val tvTitle = view.findViewById<TextView>(R.id.detail_title)
        val tvRelease = view.findViewById<TextView>(R.id.detail_release)
        val tvOverview = view.findViewById<TextView>(R.id.detail_overview)
        val chipGroup = view.findViewById<ChipGroup>(R.id.detail_genres)
        val tvRating = view.findViewById<TextView>(R.id.detail_rating)
        val container = view.findViewById<View>(R.id.detail_content)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_favorite)

        val favRepo = FavoritesRepository(requireContext())

        val title = args.title
        val overview = args.overview
        val poster = args.poster
        val release = args.releaseDate
        val vote = args.vote
        val genreIds = args.genreIds
        val movieId = args.id

        tvTitle.text = title ?: getString(R.string.app_name)
        tvOverview.text = overview.orEmpty()
        val year = release?.takeIf { it.isNotBlank() }?.substringBefore('-')
        tvRelease.text = year ?: (release ?: "")
        if (vote >= 0f) {
            tvRating.text = String.format(Locale.getDefault(), "\u2605 %.1f", vote)
            tvRating.visibility = View.VISIBLE
        } else {
            tvRating.text = ""
            tvRating.visibility = View.INVISIBLE
        }

        val genreNames = GenreUtils.namesForIds(genreIds?.toList())
        chipGroup.removeAllViews()
        val chipBg = ContextCompat.getColor(requireContext(), R.color.rating_overlay)
        val chipText = ContextCompat.getColor(requireContext(), R.color.colorOnPrimary)
        val bgState = ColorStateList.valueOf(chipBg)
        for (name in genreNames) {
            val chip = Chip(requireContext())
            chip.text = name
            chip.isClickable = false
            chip.isCheckable = false
            chip.chipBackgroundColor = bgState
            chip.setTextColor(chipText)
            chip.chipCornerRadius = resources.getDimension(R.dimen.chip_corner_radius)
            chipGroup.addView(chip)
        }

        val imageUrl = poster?.let { "https://image.tmdb.org/t/p/w780$it" }
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_launcher_background).into(ivPoster)
        } else {
            ivPoster.setImageResource(R.drawable.ic_launcher_background)
        }

        if (movieId > 0) {
            updateFabIcon(fab, favRepo.isFavorite(movieId))
            fab.setOnClickListener { v ->
                val isFav = favRepo.isFavorite(movieId)
                if (isFav) {
                    favRepo.remove(movieId)
                    updateFabIcon(fab, false)
                    Snackbar.make(v, getString(R.string.removed_from_favorites), Snackbar.LENGTH_SHORT).show()
                } else {
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
            fab.isVisible = false
        }

        container.isVisible = true
    }

    private fun updateFabIcon(fab: FloatingActionButton, isFav: Boolean) {
        fab.setImageResource(if (isFav) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
    }
}
