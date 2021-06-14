package com.worth.bluetooth.base.core.exts

import android.graphics.Rect
import android.view.View
import android.view.Window

/**
 * 隐藏系统导航、通知栏等
 */
fun Window?.hideSystemUI() {
    this?.decorView?.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
}

fun Window?.hideSystemUILater() {
    this?.decorView?.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
}

/**
 * 添加window监听 用于处理导航和通知栏相关显示操作
 */
fun Window?.addOnGlobalLayoutListener() {
    var rootBottom = Int.MIN_VALUE
    this?.decorView?.viewTreeObserver?.addOnGlobalLayoutListener {
        val r = Rect()
        this.decorView.getWindowVisibleDisplayFrame(r)
        if (rootBottom == Int.MIN_VALUE) {
            rootBottom = r.bottom
        } else {
            if (r.bottom < rootBottom) {
                this.decorView.requestFocus()
            }
            this.hideSystemUILater()
        }
    }
}
