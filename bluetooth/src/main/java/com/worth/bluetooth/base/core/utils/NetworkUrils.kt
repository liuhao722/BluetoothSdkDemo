package com.worth.bluetooth.base.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.NetworkInfo.State.CONNECTED
import android.net.NetworkInfo.State.UNKNOWN

/** 判断网络是否连接 */
fun isNetConnected(): Boolean {
    application?.applicationContext?.run {
        val info = getNetworkInfo(this)
        return info != null && info.isConnected
    }
    return false
}

@SuppressLint("MissingPermission")
private fun getNetworkInfo(context: Context): NetworkInfo? {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo
}

/**
 * 判断移动网络是否连接
 */
@SuppressLint("MissingPermission")
fun isConnectedBy4G(): Boolean {
    //获取手机的连接服务管理器，这里是连接管理器类
    val cm = application?.applicationContext?.run {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val mobileNetworkInfo = cm?.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
    var mobileState = UNKNOWN
    if (mobileNetworkInfo != null) {
        mobileState = mobileNetworkInfo.state
    }
    return CONNECTED == mobileState
}