package com.example.movieapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.model.Movie

class HorizontalMovieAdapter(
    private val onItemClick: (Movie) -> Unit = {},
) : ListAdapter<Movie, HorizontalMovieAdapter.Holder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem == newItem
        }
    }

    fun submitItems(newItems: List<Movie>) = submitList(newItems)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_small_movie, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val movie = getItem(position)
        holder.bind(movie)
        holder.itemView.setOnClickListener { onItemClick(movie) }
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val poster: ImageView = view.findViewById(R.id.small_poster)
        private val title: TextView = view.findViewById(R.id.small_title)

        fun bind(movie: Movie) {
            title.text = movie.title ?: "-"
            val url = movie.posterPath?.let { "https://image.tmdb.org/t/p/w342" + it }
            if (url != null) {
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(poster)
            } else {
                poster.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }
}
