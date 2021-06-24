package com.espressif.iot.esptouch

import android.Manifest
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.location.LocationManager.PROVIDERS_CHANGED_ACTION
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.espressif.iot.esptouch.bean.StateResult
import com.espressif.iot.esptouch.util.ByteUtil
import com.espressif.iot.esptouch.util.TouchNetUtil
import com.worth.bluetooth.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/15/21 --> 8:10 PM
 * Description: This is EspHelper
 */
object EspHelper {
    private var mSsid = ""
    private var mBssid = ""
    private var mSsidBytes: ByteArray = byteArrayOf()
    private var stateResult: StateResult? = null

    private var mTask: EsptouchTask? = null
    private var mWifiManager: WifiManager? = null
    private lateinit var mContext: Application
    private var mBroadcastData: MutableLiveData<String>? = null

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            when (action) {
                NETWORK_STATE_CHANGED_ACTION, PROVIDERS_CHANGED_ACTION -> {
                    mBroadcastData?.setValue(action)
                }
            }
        }
    }

    /**
     * 初始化基站sdk
     * @param context
     */
    fun initSdk(context: Application) {
        mContext = context
        mWifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        onWifiChanged()
        //  基站wifi部分
        mBroadcastData = MutableLiveData<String>()
        val filter = IntentFilter(NETWORK_STATE_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            filter.addAction(PROVIDERS_CHANGED_ACTION)
        }
        context?.registerReceiver(mReceiver, filter)
    }

    /**
     * 执行wifi广播
     * @param callbackWifiInfo 回调 wifi 信息
     * @param callbackBroadcast 回调 广播的结果
     * @param wifiPassword 需要用户输入wifi的密码 然后进行配对
     * @param deviceCount  设备的数量
     * @param isBroadcast   组播还是广播 true 是广播 false是组播
     */
    @JvmOverloads
    fun executeBroadcast(
        callbackWifiInfo: (StateResult?) -> Unit,
        callbackBroadcast: (List<IEsptouchResult>?) -> Unit,
        wifiPassword: String,
        deviceCount: Int = 1,
        isBroadcast: Boolean = true
    ) {
        callbackWifiInfo.invoke(stateResult)
        GlobalScope.launch(Dispatchers.IO) {
            val ssid = if (mSsidBytes == null) ByteUtil.getBytesByString(mSsid) else mSsidBytes
            val password = if (wifiPassword == null) null else ByteUtil.getBytesByString(
                wifiPassword
            )
            val bssid = TouchNetUtil.parseBssid2bytes(mBssid)
            cancel()
            mTask = EsptouchTask(ssid, bssid, password, mContext)
            mTask?.setPackageBroadcast(isBroadcast)     // if true send broadcast packets, else send multicast packets
            mTask?.setEsptouchListener {                // Result callback
                it?.run {
                    Log.e(
                        "info-Helper:",
                        "\t getBssid:" + getBssid()
                                + "\t isSuc:" + isSuc
                                + "\t isCancelled:" + isCancelled
                                + "\t getInetAddress:" + inetAddress?.toString()
                    )
                }
            }
            val result = mTask?.executeForResults(deviceCount)
            GlobalScope.launch(Dispatchers.Main) {
                callbackBroadcast.invoke(result)
            }
        }
    }

    private fun onWifiChanged() {
        stateResult = check()
        stateResult?.let {
            mSsid = it.ssid
            mSsidBytes = it.ssidBytes
            mBssid = it.bssid
            var message: CharSequence? = it.message
            if (it.wifiConnected) {
                if (it.is5G) {
                    message = mContext.getString(R.string.esptouch1_wifi_5g_message)
                    it.message = message
                }
            } else {
                cancel()
            }
        }
    }

    private fun check(): StateResult {
        var result: StateResult = checkPermission()
        if (!result.permissionGranted) {
            return result
        }
        result = checkLocation()
        result.permissionGranted = true
        if (result.locationRequirement) {
            return result
        }
        result = checkWifi()
        result.permissionGranted = true
        result.locationRequirement = false
        return result
    }

    private fun checkPermission(): StateResult {
        val result = StateResult()
        result.permissionGranted = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val locationGranted = (checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
            if (!locationGranted) {
                val splits: Array<String> =
                    mContext.getString(R.string.esptouch_message_permission)?.split("\n".toRegex())
                        .toTypedArray()
                require(splits.size == 2) { "Invalid String @RES esptouch_message_permission" }
                val ssb = SpannableStringBuilder(splits[0])
                ssb.append('\n')
                val clickMsg = SpannableString(splits[1])
                val clickSpan = ForegroundColorSpan(-0xffdd01)
                clickMsg.setSpan(clickSpan, 0, clickMsg.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                ssb.append(clickMsg)
                result.message = ssb
                return result
            }
        }
        result.permissionGranted = true
        return result
    }

    private fun checkLocation(): StateResult {
        val result = StateResult()
        result.locationRequirement = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val manager: LocationManager? = mContext.getSystemService(LocationManager::class.java)
            val enable = manager != null && LocationManagerCompat.isLocationEnabled(manager)
            if (!enable) {
                result.message = mContext.getString(R.string.esptouch_message_location)
                return result
            }
        }
        result.locationRequirement = false
        return result
    }

    private fun checkWifi(): StateResult {
        val result = StateResult()
        result.wifiConnected = false
        val wifiInfo = mWifiManager!!.connectionInfo
        val connected: Boolean = TouchNetUtil.isWifiConnected(mWifiManager, mContext)
        if (!connected) {
            result.message = mContext.getString(R.string.esptouch_message_wifi_connection)
            return result
        }
        val ssid: String = TouchNetUtil.getSsidString(mContext)
        val ipValue = wifiInfo.ipAddress
        if (ipValue != 0) {
            result.address = TouchNetUtil.getAddress(wifiInfo.ipAddress)
        } else {
            result.address = TouchNetUtil.getIPv4Address()
            if (result.address == null) {
                result.address = TouchNetUtil.getIPv6Address()
            }
        }
        result.wifiConnected = true
        result.message = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            result.is5G = TouchNetUtil.is5G(wifiInfo.frequency)
        }
        if (result.is5G) {
            result.message = mContext.getString(R.string.esptouch_message_wifi_frequency)
        }
        result.ssid = ssid
        result.ssidBytes = TouchNetUtil.getRawSsidBytesOrElse(wifiInfo, ssid.toByteArray())
        result.bssid = wifiInfo.bssid
        return result
    }

    /**
     * 取消
     */
    fun cancel() {
        mTask?.interrupt()
    }

    fun release() {
        mContext?.unregisterReceiver(mReceiver)
        mTask?.interrupt()

    }
}