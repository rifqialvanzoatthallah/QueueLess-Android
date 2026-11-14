package com.example.queueless.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.applyStatusBarTopPadding(extraDp: Int = 14) {
    val extraPx = (extraDp * resources.displayMetrics.density).toInt()
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.setPadding(
            view.paddingLeft,
            statusBarTop + extraPx,
            view.paddingRight,
            view.paddingBottom
        )
        insets
    }
}

fun View.applyStatusBarTopMargin(extraDp: Int = 16) {
    val extraPx = (extraDp * resources.displayMetrics.density).toInt()
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        val params = view.layoutParams as? ViewGroup.MarginLayoutParams
        if (params != null) {
            params.topMargin = statusBarTop + extraPx
            view.layoutParams = params
        }
        insets
    }
}