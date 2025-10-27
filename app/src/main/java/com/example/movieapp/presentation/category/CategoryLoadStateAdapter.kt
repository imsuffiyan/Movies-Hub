package com.example.movieapp.presentation.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R

class CategoryLoadStateAdapter(
    private val retry: () -> Unit,
) : LoadStateAdapter<CategoryLoadStateAdapter.LoadStateViewHolder>() {

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading_footer, parent, false)
        return LoadStateViewHolder(view, retry)
    }

    class LoadStateViewHolder(view: View, retry: () -> Unit) : RecyclerView.ViewHolder(view) {
        private val progress: ProgressBar? = view.findViewById(R.id.footer_progress)
        private val message: TextView? = view.findViewById(R.id.footer_message)
        private val retryBtn: TextView? = view.findViewById(R.id.footer_retry)

        init {
            retryBtn?.setOnClickListener { retry() }
        }

        fun bind(loadState: LoadState) {
            when (loadState) {
                is LoadState.Loading -> {
                    progress?.visibility = View.VISIBLE
                    message?.visibility = View.GONE
                    retryBtn?.visibility = View.GONE
                }
                is LoadState.Error -> {
                    progress?.visibility = View.GONE
                    message?.visibility = View.VISIBLE
                    retryBtn?.visibility = View.VISIBLE
                    message?.text = loadState.error.localizedMessage ?: "Unknown error"
                }
                is LoadState.NotLoading -> {
                    progress?.visibility = View.GONE
                    message?.visibility = View.GONE
                    retryBtn?.visibility = View.GONE
                }
            }
        }
    }
}
