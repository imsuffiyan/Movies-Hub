package com.example.movieapp.ui.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.util.GenreUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieDetailFragment : Fragment(R.layout.fragment_movie_detail) {

    private val viewModel: MovieDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.detail_toolbar)
        toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireContext(),
            androidx.appcompat.R.drawable.abc_ic_ab_back_material,
        )
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val ivPoster = view.findViewById<ImageView>(R.id.detail_poster)
        val tvTitle = view.findViewById<TextView>(R.id.detail_title)
        val tvRelease = view.findViewById<TextView>(R.id.detail_release)
        val tvOverview = view.findViewById<TextView>(R.id.detail_overview)
        val chipGroup = view.findViewById<ChipGroup>(R.id.detail_genres)
        val tvRating = view.findViewById<TextView>(R.id.detail_rating)
        val container = view.findViewById<View>(R.id.detail_content)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_favorite)

        fab.isVisible = false
        container.isVisible = false

        fab.setOnClickListener { viewModel.toggleFavorite() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        toolbar.title = state.title ?: getString(R.string.app_name)

                        tvTitle.text = state.title ?: getString(R.string.app_name)
                    tvOverview.text = state.overview.orEmpty()
                    val year = state.releaseDate?.takeIf { it.isNotBlank() }?.substringBefore('-')
                    tvRelease.text = year ?: (state.releaseDate ?: "")
                    if (state.voteAverage >= 0f) {
                        tvRating.text = String.format(Locale.getDefault(), "\u2605 %.1f", state.voteAverage)
                        tvRating.visibility = View.VISIBLE
                    } else {
                        tvRating.text = ""
                        tvRating.visibility = View.INVISIBLE
                    }

                    val genreNames = GenreUtils.namesForIds(state.genreIds.takeIf { it.isNotEmpty() })
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

                    val imageUrl = state.posterPath?.let { "https://image.tmdb.org/t/p/w780$it" }
                    if (imageUrl != null) {
                        Glide.with(this@MovieDetailFragment)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(ivPoster)
                    } else {
                        ivPoster.setImageResource(R.drawable.ic_launcher_background)
                    }

                    if (state.hasValidId) {
                        fab.isVisible = true
                        updateFabIcon(fab, state.isFavorite)
                    } else {
                        fab.isVisible = false
                    }

                    container.isVisible = true
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is MovieDetailEvent.FavoriteToggled -> {
                            val message = if (event.isFavorite) {
                                getString(R.string.added_to_favorites)
                            } else {
                                getString(R.string.removed_from_favorites)
                            }
                            Snackbar.make(fab, message, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateFabIcon(fab: FloatingActionButton, isFav: Boolean) {
        fab.setImageResource(if (isFav) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
    }
}
