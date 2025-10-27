package com.example.movieapp.paging

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.movieapp.R
import com.example.movieapp.model.Movie
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import java.util.Locale

class CategoryPagingAdapter(
    private val onItemClick: (Movie) -> Unit
) : PagingDataAdapter<Movie, CategoryPagingAdapter.Holder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem == newItem
        }

        private const val IMAGE_BASE = "https://image.tmdb.org/t/p/w342"
        private val GENRE_MAP = mapOf(
            28 to "Action",
            12 to "Adventure",
            16 to "Animation",
            35 to "Comedy",
            80 to "Crime",
            99 to "Documentary",
            18 to "Drama",
            10751 to "Family",
            14 to "Fantasy",
            36 to "History",
            27 to "Horror",
            10402 to "Music",
            9648 to "Mystery",
            10749 to "Romance",
            878 to "Science Fiction",
            10770 to "TV Movie",
            53 to "Thriller",
            10752 to "War",
            37 to "Western"
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val movie = getItem(position) ?: return
        holder.bind(movie)
        holder.itemView.setOnClickListener { onItemClick(movie) }
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val poster: ShapeableImageView = view.findViewById(R.id.poster)
        private val shimmerContainer: View = view.findViewById(R.id.poster_shimmer)
        private val title: TextView = view.findViewById(R.id.title)
        private val overview: TextView = view.findViewById(R.id.overview)
        private val rating: TextView = view.findViewById(R.id.rating)
        private val releaseYearTv: TextView = view.findViewById(R.id.release_year)
        private val genresGroup: ChipGroup = view.findViewById(R.id.genres)
        private var shimmerAnimator: ObjectAnimator? = null

        fun bind(item: Movie) {
            val ctx = itemView.context
            val dash = ctx.getString(R.string.dash)

            title.text = item.title ?: ctx.getString(R.string.app_name)
            overview.text = item.overview ?: ""
            rating.text = item.voteAverage?.let { String.format(Locale.getDefault(), "★ %.1f", it) } ?: dash
            val year = item.releaseDate?.takeIf { it.isNotBlank() }?.substringBefore('-')
            releaseYearTv.text = year ?: ""

            // genres
            val genreNames = (item.genreIds ?: emptyList()).mapNotNull { GENRE_MAP[it] }
            genresGroup.removeAllViews()
            val chipBgColor = ContextCompat.getColor(ctx, R.color.rating_overlay)
            val chipTextColor = ContextCompat.getColor(ctx, R.color.colorOnPrimary)
            val bgState = ColorStateList.valueOf(chipBgColor)
            val textSizePx = ctx.resources.getDimension(R.dimen.chip_text_size)
            for (name in genreNames) {
                val chip = Chip(ctx)
                chip.text = name
                chip.isClickable = false
                chip.isCheckable = false
                chip.chipBackgroundColor = bgState
                chip.setTextColor(chipTextColor)
                chip.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx)
                genresGroup.addView(chip)
            }

            // shimmer
            shimmerAnimator?.cancel()
            shimmerContainer.alpha = 1f
            shimmerAnimator = ObjectAnimator.ofFloat(shimmerContainer, "alpha", 0.6f, 1f).apply {
                duration = 800
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }

            val url = item.posterPath?.let { IMAGE_BASE + it }
            if (url != null) {
                Glide.with(ctx)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean
                        ): Boolean {
                            shimmerAnimator?.cancel()
                            shimmerContainer.alpha = 1f
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean
                        ): Boolean {
                            shimmerAnimator?.cancel()
                            shimmerContainer.alpha = 1f
                            return false
                        }
                    })
                    .into(poster)
            } else {
                shimmerAnimator?.cancel()
                shimmerContainer.alpha = 1f
                poster.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }
}

