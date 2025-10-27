package com.example.movieapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.domain.model.Movie
import com.example.movieapp.domain.model.MovieCategory
import com.example.movieapp.presentation.home.SectionUiModel

class SectionAdapter(
    private val viewPool: RecyclerView.RecycledViewPool,
    private val onSeeAll: (category: MovieCategory, title: String) -> Unit,
    private val onItemClick: (Movie) -> Unit,
) : ListAdapter<SectionUiModel, SectionAdapter.Holder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SectionUiModel>() {
            override fun areItemsTheSame(oldItem: SectionUiModel, newItem: SectionUiModel): Boolean =
                oldItem.category == newItem.category

            override fun areContentsTheSame(oldItem: SectionUiModel, newItem: SectionUiModel): Boolean =
                oldItem == newItem
        }
    }

    fun submitSections(newSections: List<SectionUiModel>) {
        submitList(newSections.map { it.copy(movies = it.movies.toList()) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTv: TextView = view.findViewById(R.id.section_title)
        private val seeAllTv: TextView = view.findViewById(R.id.section_see_all)
        private val innerRv: RecyclerView = view.findViewById(R.id.section_recycler)
        private val innerAdapter = HorizontalMovieAdapter(onItemClick)
        private val loadingView: View = view.findViewById(R.id.section_loading)
        private val errorView: TextView = view.findViewById(R.id.section_error)

        init {
            innerRv.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            innerRv.adapter = innerAdapter
            innerRv.setRecycledViewPool(viewPool)
            innerRv.setHasFixedSize(true)
            innerRv.isNestedScrollingEnabled = false
            innerRv.setItemViewCacheSize(10)
        }

        fun bind(section: SectionUiModel) {
            titleTv.text = section.title
            seeAllTv.setOnClickListener { onSeeAll(section.category, section.title) }
            loadingView.isVisible = section.isLoading
            val hasError = !section.isLoading && section.errorMessage != null
            errorView.isVisible = hasError
            if (hasError) {
                val fallback = itemView.context.getString(R.string.error_loading_section, section.title)
                errorView.text = section.errorMessage ?: fallback
            }
            innerRv.isVisible = !section.isLoading && !hasError
            innerAdapter.submitItems(section.movies)
        }
    }
}
