package com.example.movieapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.model.Section
import com.example.movieapp.model.Movie

class SectionAdapter(
    private val viewPool: RecyclerView.RecycledViewPool,
    private val onSeeAll: (category: String, title: String) -> Unit,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<SectionAdapter.Holder>() {

    private val sections = mutableListOf<Section>()

    fun updateSections(newSections: List<Section>) {
        sections.clear()
        sections.addAll(newSections)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(sections[position])
    }

    override fun getItemCount(): Int = sections.size

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTv: TextView = view.findViewById(R.id.section_title)
        private val seeAllTv: TextView = view.findViewById(R.id.section_see_all)
        private val innerRv: RecyclerView = view.findViewById(R.id.section_recycler)
        private val innerAdapter = HorizontalMovieAdapter(emptyList(), onItemClick)

        init {
            innerRv.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            innerRv.adapter = innerAdapter
            // Performance tweaks for nested recyclers
            innerRv.setRecycledViewPool(viewPool)
            innerRv.setHasFixedSize(true)
            innerRv.isNestedScrollingEnabled = false
            innerRv.setItemViewCacheSize(10)
        }

        fun bind(section: Section) {
            titleTv.text = section.title
            seeAllTv.setOnClickListener { onSeeAll(section.category, section.title) }
            innerAdapter.update(section.movies)
        }
    }
}
