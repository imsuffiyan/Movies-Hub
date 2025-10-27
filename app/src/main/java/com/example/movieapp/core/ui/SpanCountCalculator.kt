package com.example.movieapp.core.ui

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.GridLayoutManager
import kotlin.math.max

fun calculateSpanCount(context: Context, minItemWidthDp: Int): Int {
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    val spanCount = (screenWidthDp / minItemWidthDp).toInt()
    return max(1, spanCount)
}

fun createResponsiveGridLayoutManager(
    context: Context,
    minItemWidthDp: Int,
): GridLayoutManager {
    val spanCount = calculateSpanCount(context, minItemWidthDp)
    return GridLayoutManager(context, spanCount)
}
