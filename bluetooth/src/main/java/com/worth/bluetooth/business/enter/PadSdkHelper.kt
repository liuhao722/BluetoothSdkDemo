package com.worth.bluetooth.business.enter

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.worth.bluetooth.business.ext.setMacId
import com.worth.bluetooth.business.ext.setPhoneType
import com.worth.bluetooth.business.utils.BluetoothUtil
import com.worth.bluetooth.business.utils.BluetoothUtils
import com.worth.framework.base.core.storage.MeKV
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
        isPad?.let { MeKV.setPhoneType(it) }
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
     * 直接打开蓝牙
     */
    fun onBlueTooth() {
        BluetoothUtil.instance.onBlueTooth()
    }

    /**
     * 关闭蓝牙
     */
    fun offBlueTooth() {
        BluetoothUtil.instance.offBlueTooth()
    }

    /**
     * 去设置页面打开蓝牙操作
     */
    fun toSettingBluetooth(activity: Activity) {
        BluetoothUtil.instance.toSettingBluetooth(activity)
    }

    /**
     * 蓝牙是否可用
     */
    fun checkBlueToothEnable(): Boolean = BluetoothUtil.instance.checkBlueToothEnable()

    /**
     * 获取已经配对的设备
     */
    val connectedDevices: Set<BluetoothDevice>?
        get() = BluetoothUtil.instance.connectedDevices

    /**
     * 可发现模式
     * 默认情况下，设备的可发现模式会持续120秒。
     * 通过给Intent对象添加EXTRA_DISCOVERABLE_DURATION附加字段，可以定义不同持续时间。目前设置300秒
     * 应用程序能够设置的最大持续时间是3600秒
     */
    fun discoverableDuration(activity: Activity) {
        BluetoothUtil.instance.discoverableDuration(activity)
    }

    /**
     * 扫描蓝牙，会走广播
     */
    fun startDiscovery() {
        BluetoothUtil.instance.startDiscovery()
    }

    /**
     * 停止扫描
     */
    fun stopDiscovery() {
        BluetoothUtil.instance.stopDiscovery()
    }

    /**
     * 连接设备
     */
    fun connectGatt(context: Context, device: BluetoothDevice) {
        application?.let { BluetoothUtil.instance.connectGatt(it, device) }
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
