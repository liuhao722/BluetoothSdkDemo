package com.espressif.iot.esptouch

import android.app.Application
import com.worth.framework.base.core.exts.nullTo

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/15/21 --> 8:10 PM
 * Description: This is EspHelper
 */
object EspHelper {
    lateinit var task: EsptouchTask

    /**
     * 初始化基站sdk
     * @param context
     * @param callback
     */
    fun initSdk(context: Application, callback: () -> Unit) {
        val apSsid = byteArrayOf()          // Set AP's SSID
        val apBssid = byteArrayOf()         // Set AP's BSSID
        val apPassword = byteArrayOf()      // Set AP's password
        task.nullTo {
            task = EsptouchTask(apSsid, apBssid, apPassword, context)
            task
        }
        task = EsptouchTask(apSsid, apBssid, apPassword, context)
        task?.setPackageBroadcast(true)     // if true send broadcast packets, else send multicast packets

        task?.setEsptouchListener {
            it?.run {

            }
            // Result callback
        }
    }

    /**
     * 执行task
     */
    fun executeTask() {
        val expectResultCount = 1
        task?.executeForResults(expectResultCount)?.let {
            val first = it[0]
            if (first.isCancelled) {
                // User cancel the task
                return
            }
            if (first.isSuc) {
                // EspTouch successfully
            }
        }
    }

    /**
     * 取消
     */
    fun cancel() {
        task?.interrupt()
    }
}