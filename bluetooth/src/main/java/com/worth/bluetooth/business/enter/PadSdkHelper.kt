package com.worth.bluetooth.business.enter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.worth.bluetooth.business.ext.setMacId
import com.worth.framework.base.core.storage.MeKV
import com.worth.framework.base.core.utils.L
import com.worth.framework.base.core.utils.application
import top.wuhaojie.bthelper.*


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/25/21 --> 4:45 PM
 * Description: This is VipSdkHelper
 */
class PadSdkHelper private constructor() {
    private val TAG = "PadSdkHelper"
    var btHelperClient: BtHelperClient? = null

    /**
     * 初始化sdk
     */
    @JvmOverloads
    fun initPadSdk(): PadSdkHelper {
        var btHelperClient = BtHelperClient.from(application)

        return this
    }

    /**
     * 设置使用设备的类型
     */
    fun setPhoneType(isPad: Boolean = true): PadSdkHelper {

        return this
    }

    /**
     * 设置macId
     */
    fun setMacId(macId: String?): PadSdkHelper {
        macId?.let { MeKV.setMacId(it) }
        return this
    }

    /**
     * 连接设备
     */
    fun connection() {

    }

    /**
     * 断开设备
     */
    fun disconnection() {

    }

    /**
     * 搜索设备
     */
    fun searchDevices() {
        btHelperClient?.searchDevices(object : OnSearchDeviceListener {
            override fun onStartDiscovery() {
                // 在进行搜索前回调
                L.d(TAG, "onStartDiscovery()")
            }

            @SuppressLint("MissingPermission")
            override fun onNewDeviceFound(device: BluetoothDevice) {
                // 当寻找到一个新设备时回调
                L.d(TAG, "device: ${device.name} ${device.address}")
            }

            override fun onSearchCompleted(
                bondedList: List<BluetoothDevice>,
                newList: List<BluetoothDevice>
            ) {
                // 当搜索蓝牙设备完成后回调
                L.d(TAG, "SearchCompleted: bondedList$bondedList")
                L.d(TAG, "SearchCompleted: newList$newList")
            }

            override fun onError(e: java.lang.Exception) {
                e.printStackTrace()
            }
        })
    }

    /**
     * @param macId mac地址 "20:15:03:18:08:63"
     * @param msg 要发送的内容
     */
    fun sendMsg(macId: String, msg: String) {
        val item = MessageItem(msg)

        btHelperClient?.sendMessage(macId, item, true, object : OnSendMessageListener {
            override fun onSuccess(status: Int, response: String) {
                // 当发送成功，同时获得响应体时回调

                // 状态码:   描述响应是否正确.
                //           1代表响应回复内容正确, -1代表响应内容不正确, 即数据损坏
                // 响应信息: 来自远程蓝牙设备的响应内容, 可以通过response.getBytes()获取字节数组
            }

            override fun onConnectionLost(e: Exception) {
                e.printStackTrace()
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        })
    }

    /**
     *  设置过滤器 使用过滤器来过滤掉那些硬件设备出现差错的数据
     */
    fun filter() {
        btHelperClient!!.setFilter { response -> response.trim { it <= ' ' }.length >= 5 }
    }

    /**
     * 释放资源
     */
    fun release() {
        btHelperClient?.close()
    }

    /**
     * 对象单例
     */
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = PadSdkHelper()
    }
}
