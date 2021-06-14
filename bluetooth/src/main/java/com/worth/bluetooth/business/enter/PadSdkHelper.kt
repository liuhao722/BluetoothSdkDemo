package com.worth.bluetooth.business.enter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.worth.bluetooth.business.ext.setMacId
import com.worth.bluetooth.business.utils.BluetoothUtils
import com.worth.bluetooth.other.BtHelperClient
import com.worth.bluetooth.other.MessageItem
import com.worth.bluetooth.other.OnSearchDeviceListener
import com.worth.bluetooth.other.OnSendMessageListener
import com.worth.framework.base.core.storage.MeKV
import com.worth.framework.base.core.utils.L
import com.worth.framework.base.core.utils.application


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/25/21 --> 4:45 PM
 * Description: This is VipSdkHelper
 */
class PadSdkHelper private constructor() {
    private val TAG = "PadSdkHelper"
    /**
     * 初始化sdk
     */
    @JvmOverloads
    fun initPadSdk(): PadSdkHelper {
        BluetoothUtils.instance.initSdk()
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
        BluetoothUtils.instance.connection()
    }

    /**
     * 断开设备
     */
    fun disconnection() {
        BluetoothUtils.instance.disconnection()
    }

    /**
     * 搜索设备
     * 可以获取到蓝牙的名称和物理地址，在未连接之前，拿不到uuid。
     */
    fun searchDevices() {
        BluetoothUtils.instance.searchDevices()
    }

    /**
     * @param macId mac地址 "20:15:03:18:08:63"
     * @param msg 要发送的内容
     */
    fun sendMsg(macId: String, msg: String) {
        BluetoothUtils.instance.sendMsg(macId, msg)
    }

    /**
     *  设置过滤器 使用过滤器来过滤掉那些硬件设备出现差错的数据
     */
    fun filter() {
        BluetoothUtils.instance.filter()
    }

    /**
     * 释放资源
     */
    fun release() {
        BluetoothUtils.instance.release()
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
