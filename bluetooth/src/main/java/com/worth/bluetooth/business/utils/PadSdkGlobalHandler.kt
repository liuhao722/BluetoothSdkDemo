package com.worth.bluetooth.business.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/30/21 --> 10:34 PM
 * Description: This is GlobalHandler
 */
internal class PadSdkGlobalHandler private constructor() {
    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {

            }
        }
    }
    internal var mHandler =
        WeakReference(handler)

    private object SingletonHolder {
        var instance = PadSdkGlobalHandler()
    }

    companion object {
        private const val TAG = "PadSdkGlobalHandler"
        fun ins(): PadSdkGlobalHandler {
            return SingletonHolder.instance
        }
    }
}