package com.worth.bluetooth.base.core.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

val mLayoutInflater: LayoutInflater?
    get() = LayoutInflater.from(application?.applicationContext)


/**
 * 加载视图
 */
fun inflateView(@LayoutRes layoutResId: Int?, rootView: ViewGroup? = null): View {
    return layoutResId?.let { mLayoutInflater?.inflate(it, rootView, false) }
        ?: View(application?.applicationContext)
}