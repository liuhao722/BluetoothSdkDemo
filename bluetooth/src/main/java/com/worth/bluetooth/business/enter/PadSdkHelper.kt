package com.worth.bluetooth.business.enter

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.provider.Settings
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanAndConnectCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.worth.bluetooth.business.ext.getMacId
import com.worth.bluetooth.business.ext.setMacId
import com.worth.bluetooth.business.ext.setPhoneType
import com.worth.bluetooth.business.gloable.SERVICE_UUID
import com.worth.bluetooth.business.utils.BluetoothUtil
import com.worth.bluetooth.business.utils.PadSdkExt
import com.worth.framework.base.core.storage.MeKV
import com.worth.framework.base.core.utils.L
import com.worth.framework.base.core.utils.application
import java.util.*


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/25/21 --> 4:45 PM
 * Description: This is VipSdkHelper
 */
class PadSdkHelper private constructor() {
    private val isAutoConnect: Boolean = false  //  是否自动重连
    private val TAG = "PadSdkHelper"
    private val context = application

    /**
     * 初始化sdk
     */
    @JvmOverloads
    fun initPadSdk(): PadSdkHelper {
        BleManager.getInstance().init(context)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setSplitWriteNum(20)
            .setConnectOverTime(10000)
            .operateTimeout = 5000

        return this
    }

    /**
     * 配置扫描规则
     */
    internal fun initScanRule() {
        val serviceUUIDS = arrayOf<UUID>(UUID.fromString(SERVICE_UUID))
        val names = "iMEMBER"
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setServiceUuids(serviceUUIDS)
            .setDeviceName(true, names)     //  模糊匹配--看是否是过滤掉names 还是说只显示names
            .setDeviceMac(MeKV.getMacId())
            .setAutoConnect(isAutoConnect)
            .setScanTimeOut(10000)
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)
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
    fun connect(bleDevice: BleDevice) {
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                L.d(TAG, "onStartConnect")
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                L.d(TAG, "onConnectFail：${exception.code}:\t${exception.description}")
            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                L.d(TAG, "onConnectSuccess：${status}")
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                L.d(
                    TAG, "onDisConnected：${status}:\t " +
                            "isActiveDisConnected:$isActiveDisConnected"
                )
            }
        })
    }

    /**
     * 连接设备
     * @param macID 链接的mac地址
     */
    fun connect(macID: String) {
        BleManager.getInstance().connect(macID, object : BleGattCallback() {
            override fun onStartConnect() {}
            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {}
            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {}
            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {

            }
        })
    }

    fun scanAndConnect(callback: BleScanAndConnectCallback){
        BleManager.getInstance().scanAndConnect(object : BleScanAndConnectCallback() {
            override fun onScanStarted(success: Boolean) {

            }

            override fun onScanning(bleDevice: BleDevice?) {

            }

            override fun onScanFinished(scanResult: BleDevice) {
                //  调用cancelScan之后会回调该处
            }

            override fun onStartConnect() {

            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {

            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {

            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {

            }
        })
    }

    /**
     * 取消扫描
     * 如果调用该方法，如果还在扫描状态，则立即结束，并回调该onScanFinished方法。
     */
    fun cancelScan(){
        BleManager.getInstance().cancelScan();
    }
    /**
     * 断开连接设备
     */
    fun disconnect(bleDevice: BleDevice?) {
        BleManager.getInstance().disconnect(bleDevice)
    }
    /**
     * 断开所有已连接设备
     */
    fun disconnectAllDevice() {
        BleManager.getInstance().disconnectAllDevice()
    }


















    /**
     * 搜索设备
     * 可以获取到蓝牙的名称和物理地址，在未连接之前，拿不到uuid。
     */
    fun searchDevices() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {

            }

            override fun onScanning(bleDevice: BleDevice) {

            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {

            }
        })
    }

    /**
     * @param macId mac地址 "20:15:03:18:08:63"
     * @param msg 要发送的内容
     */
    fun sendMsg(macId: String, msg: String) {

    }

    /**
     *  设置过滤器 使用过滤器来过滤掉那些硬件设备出现差错的数据
     */
    fun filter() {

    }

    /**
     * 直接打开蓝牙
     */
    fun onBlueTooth() {
        if (BleManager.getInstance().isSupportBle) {
            BleManager.getInstance().enableBluetooth()
        }
    }

    /**
     * 关闭蓝牙
     */
    fun offBlueTooth() {
        if (BleManager.getInstance().isSupportBle) {
            BleManager.getInstance().disableBluetooth()
        }
    }

    /**
     * 去设置页面打开蓝牙操作
     */
    fun toSettingBluetooth(activity: Activity) {
        if (BleManager.getInstance().isSupportBle) {
            val blueTooth = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            activity.startActivity(blueTooth)
        }
    }

    /**
     * 蓝牙是否可用
     */
    fun checkBlueToothEnable(): Boolean = BleManager.getInstance().isSupportBle

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
     * 清理蓝牙缓存
     */
    fun refreshDeviceCache(): Boolean {
        return BluetoothUtil.instance.refreshDeviceCache()
    }

    /**
     * 释放资源
     */
    fun release() {
        BleManager.getInstance().destroy()
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
