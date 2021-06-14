package com.worth.bluetooth.base.core.utils

import android.graphics.drawable.Drawable
import androidx.annotation.*

object ResourceUtils {

    @JvmStatic
    fun getDimens(@DimenRes dimensId: Int): Int? {
        return application?.resources?.getDimension(dimensId)?.toInt()
    }

    @JvmStatic
    fun getColor(@ColorRes colorId: Int): Int? {
        return application?.resources?.getColor(colorId)
    }

    @JvmStatic
    fun getScreenWidth(): Int? {
        return application?.resources?.displayMetrics?.widthPixels
    }

    @JvmStatic
    fun getString(@StringRes strId: Int): String? {
        return application?.resources?.getString(strId)
    }

    @JvmStatic
    fun getStringArray(@ArrayRes arrayId: Int): Array<String>? {
        return application?.resources?.getStringArray(arrayId)
    }

    @JvmStatic
    fun getIntArray(@ArrayRes arrayId: Int): IntArray? {
        return application?.resources?.getIntArray(arrayId)
    }

    @JvmStatic
    fun getDrawale(@DrawableRes resId: Int): Drawable? {
        return application?.resources?.getDrawable(resId)
    }

    @JvmStatic
    fun stringFormat(@StringRes str: Int, value: Float): String? {
        getString(str)?.run {
            return String.format(this, value)
        }
        return null
    }

    @JvmStatic
    fun stringFormat(@StringRes str: Int, value: Int): String? {
        getString(str)?.run {
            return String.format(this, value)
        }
        return null
    }

    @JvmStatic
    fun stringFormat(@StringRes str: Int, value1: Int? = null, value2: Int? = null): String? {
        getString(str)?.run {
            return String.format(this, value1, value2)
        }
        return null
    }

    @JvmStatic
    fun stringFormat(@StringRes str: Int, value: String?): String? {
        getString(str)?.run {
            return String.format(this, value)
        }
        return null
    }

    @JvmStatic
    fun stringFormat(@StringRes str: Int, value1: String? = null, value2: String? = null): String? {
        getString(str)?.run {
            return String.format(this, value1, value2)
        }
        return null
    }

    @JvmStatic
    fun stringFormat(
        @StringRes str: Int,
        value1: String?,
        value2: String?,
        value3: String?,
        value4: String?
    ): String? {
        getString(str)?.run {
            return String.format(this, value1, value2, value3, value4)
        }
        return null
    }
}