package com.example.movieapp.ui

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
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

class MovieAdapter(
    initialItems: List<Movie> = emptyList(),
    private val onItemClick: (Movie) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
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

        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    // mutable list so we can append
    private val items = initialItems.toMutableList()
    private var loadingFooter = false

    fun update(newItems: List<Movie>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size
            override fun getNewListSize(): Int = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].id == newItems[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = items[oldItemPosition]
                val new = newItems[newItemPosition]
                return old == new
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) = null
        })
        this.items.clear()
        this.items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    fun append(newItems: List<Movie>) {
        if (newItems.isEmpty()) return
        val start = items.size
        this.items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    fun showLoading(show: Boolean) {
        if (show == loadingFooter) return
        if (show) {
            // insert footer at end (index == current items.size)
            loadingFooter = true
            notifyItemInserted(items.size)
        } else {
            // remove footer at previous footer index (which is current items.size)
            val footerIndex = items.size
            loadingFooter = false
            notifyItemRemoved(footerIndex)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < items.size) VIEW_TYPE_ITEM else VIEW_TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
            Holder(view, onItemClick, items)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading_footer, parent, false)
            LoadingHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Holder) holder.bind(items[position])
        else if (holder is LoadingHolder) holder.bind()
    }

    override fun getItemCount(): Int = items.size + if (loadingFooter) 1 else 0

    inner class Holder(view: View, private val click: (Movie) -> Unit, private val itemsRef: List<Movie>) : RecyclerView.ViewHolder(view) {
        private val poster: ShapeableImageView = view.findViewById(R.id.poster)
        private val shimmerContainer: View = view.findViewById(R.id.poster_shimmer)
        private val title: TextView = view.findViewById(R.id.title)
        private val overview: TextView = view.findViewById(R.id.overview)
        private val rating: TextView = view.findViewById(R.id.rating)
        private val releaseYearTv: TextView = view.findViewById(R.id.release_year)
        private val genresGroup: ChipGroup = view.findViewById(R.id.genres)
        private var shimmerAnimator: ObjectAnimator? = null

        init {
            // Handle item click: delegate to onItemClick lambda with the current item
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    click(itemsRef[position])
                }
            }
        }

        fun bind(item: Movie) {
            val ctx = itemView.context
            val dash = ctx.getString(R.string.dash)

            title.text = item.title ?: ctx.getString(R.string.app_name)
            overview.text = item.overview ?: ""

            // Rating (locale-aware)
            rating.text = item.voteAverage?.let { String.format(Locale.getDefault(), "★ %.1f", it) } ?: dash

            // Release year (below genres)
            val year = item.releaseDate?.takeIf { it.isNotBlank() }?.substringBefore('-')
            releaseYearTv.text = year ?: ""

            // Genres: map ids to names
            val genreNames = (item.genreIds ?: emptyList()).mapNotNull { id ->
                Companion.GENRE_MAP[id]
            }

            // Populate ChipGroup: clear previous chips then add new ones
            genresGroup.removeAllViews()
            val chipBgColor = ContextCompat.getColor(ctx, R.color.rating_overlay)
            val chipTextColor = ContextCompat.getColor(ctx, R.color.colorOnPrimary)
            val bgState = ColorStateList.valueOf(chipBgColor)
            // corner radius set by theme / default; avoid deprecated setter
            val textSizePx = ctx.resources.getDimension(R.dimen.chip_text_size)

            for (name in genreNames) {
                val chip = Chip(ctx)
                chip.text = name
                chip.isClickable = false
                chip.isCheckable = false
                chip.chipBackgroundColor = bgState
                chip.setTextColor(chipTextColor)
                // rely on theme default corner radius to avoid deprecated API
                // set text size using pixels
                chip.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx)
                genresGroup.addView(chip)
            }

            // Prepare shimmer animator (simple pulsing alpha) and start it
            shimmerAnimator?.cancel()
            shimmerContainer.alpha = 1f
            shimmerAnimator = ObjectAnimator.ofFloat(shimmerContainer, "alpha", 0.6f, 1f).apply {
                duration = 800
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }

            // Poster image into ShapeableImageView with shimmer fallback
            val url = item.posterPath?.let { IMAGE_BASE + it }
            if (url != null) {
                Glide.with(ctx)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            // stop shimmer on failure
                            shimmerAnimator?.cancel()
                            shimmerContainer.alpha = 1f
                            return false // allow Glide to set placeholder
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            // stop shimmer when ready
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

    inner class LoadingHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val progress: ProgressBar? = view.findViewById(R.id.footer_progress)
        fun bind() {
            // Nothing to bind for now; progress bar will animate
            progress?.isIndeterminate = true
        }
    }
}
