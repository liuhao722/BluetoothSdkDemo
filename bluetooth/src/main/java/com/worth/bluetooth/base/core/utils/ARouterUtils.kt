package com.worth.bluetooth.base.core.utils

import com.alibaba.android.arouter.launcher.ARouter

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/1/21 --> 10:26 AM
 * Description: This is ARouterUtils
 */

const val FIRST_ARGS = "first_args"
const val SECOND_ARGS = "second_args"

@JvmOverloads
fun openPath(path: String, firstArgs: String?, secondArgs: String?) {
    ARouter.getInstance()
        .build(path)
        .withString(FIRST_ARGS, firstArgs)
        .withString(SECOND_ARGS, secondArgs)
        .navigation()
}

@JvmOverloads
fun openPath(path: String, map: MutableMap<String, Any>?) {
    val aRouter = ARouter.getInstance().build(path)
    map?.forEach { item ->
        aRouter.withObject(item.key, item.value)
    }
    aRouter.navigation()
}


