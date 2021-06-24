package com.espressif.iot.esptouch

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager.PROVIDERS_CHANGED_ACTION
import android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION
import android.os.Build
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.worth.framework.base.core.exts.nullTo
import com.worth.framework.base.core.utils.application

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/15/21 --> 8:10 PM
 * Description: This is EspHelper
 */
object EspHelper {
    lateinit var task: EsptouchTask

    private var mBroadcastData: MutableLiveData<String>? = null

    private var mCacheMap: MutableMap<String, Any?>  = mutableMapOf()

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            when (action) {
                NETWORK_STATE_CHANGED_ACTION, PROVIDERS_CHANGED_ACTION ->{
                    mBroadcastData?.setValue(action)
                }
            }
        }
    }

    /**
     * 初始化基站sdk
     * @param context
     * @param callback
     */
    fun initSdk(context: Application, callback: (IEsptouchResult) -> Unit) {
        val apSsid = byteArrayOf()          // Set AP's SSID
        val apBssid = byteArrayOf()         // Set AP's BSSID
        val apPassword = byteArrayOf()      // Set AP's password
        task.nullTo {
            task = EsptouchTask(apSsid, apBssid, apPassword, context)
            task
        }
        task = EsptouchTask(apSsid, apBssid, apPassword, context)
        task?.setPackageBroadcast(true)     // if true send broadcast packets, else send multicast packets

        task?.setEsptouchListener {   // Result callback
            it?.run {
                callback.invoke(this)
            }
        }

        //  基站wifi部分
        mBroadcastData = MutableLiveData<String>()
        val filter = IntentFilter(NETWORK_STATE_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            filter.addAction(PROVIDERS_CHANGED_ACTION)
        }
        application?.registerReceiver(mReceiver, filter)
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

    fun observeBroadcast(owner: LifecycleOwner, observer: Observer<String?>) {
        mBroadcastData?.observe(owner, observer)
    }

    fun observeBroadcastForever(observer: Observer<String?>) {
        mBroadcastData?.observeForever(observer)
    }

    fun removeBroadcastObserver(observer: Observer<String?>) {
        mBroadcastData?.removeObserver(observer)
    }

    fun putCache(key: String, value: Any?) {
        mCacheMap[key] = value
    }

    fun takeCache(key: String?): Any? {
        return mCacheMap.remove(key)
    }

    /**
     * 取消
     */
    fun cancel() {
        task?.interrupt()
    }

    fun release() {
        application?.unregisterReceiver(mReceiver)
    }
}