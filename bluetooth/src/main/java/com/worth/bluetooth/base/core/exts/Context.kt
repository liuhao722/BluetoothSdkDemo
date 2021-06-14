package com.worth.bluetooth.base.core.exts

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.*
import com.worth.bluetooth.base.core.utils.application

fun Context.inflate(@LayoutRes layoutResId: Int): View {
    return LayoutInflater.from(this).inflate(layoutResId, null)
}

fun Context.getDimens(@DimenRes dimensId: Int): Int? {
    return application?.resources?.getDimension(dimensId)?.toInt()
}

fun Context.getColor(@ColorRes colorId: Int): Int? {
    return application?.resources?.getColor(colorId)
}

fun Context.getString(@StringRes strId: Int): String? {
    return application?.resources?.getString(strId)
}

fun Context.getStringArray(@ArrayRes arrayId: Int): Array<String>? {
    return application?.resources?.getStringArray(arrayId)
}

fun Context.getIntArray(@ArrayRes arrayId: Int): IntArray? {
    return application?.resources?.getIntArray(arrayId)
}

fun Context.getDrawale(@DrawableRes resId: Int): Drawable? {
    return application?.resources?.getDrawable(resId)
}

fun Context.stringFormat(@StringRes str: Int, value: Float): String? {
    getString(str)?.run {
        return String.format(this, value)
    }
    return null
}

fun Context.stringFormat(@StringRes str: Int, value: Int): String? {
    getString(str)?.run {
        return String.format(this, value)
    }
    return null
}

fun Context.stringFormat(@StringRes str: Int, value1: Int, value2: Int): String? {
    getString(str)?.run {
        return String.format(this, value1, value2)
    }
    return null
}

fun Context.stringFormat(@StringRes str: Int, value: String?): String? {
    getString(str)?.run {
        return String.format(this, value)
    }
    return null
}

fun Context.stringFormat(@StringRes str: Int, value1: String?, value2: String?): String? {
    getString(str)?.run {
        return String.format(this, value1, value2)
    }
    return null
}

fun Context.stringFormat(@StringRes str: Int, value1: String?, value2: String?, value3: String?, value4: String?): String? {
    getString(str)?.run {
        return String.format(this, value1, value2, value3, value4)
    }
    return null
}

/**
 * 显示或隐藏软键盘
 */
fun Context?.showOrHideKeyboard(view: View?, isShow: Boolean = false) {
    this?.run {
        if (view != null) {
            (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?)?.run {
                if (isShow) {
                    showSoftInput(view, InputMethodManager.RESULT_UNCHANGED_SHOWN)
                } else {
                    hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        }
    }
}


