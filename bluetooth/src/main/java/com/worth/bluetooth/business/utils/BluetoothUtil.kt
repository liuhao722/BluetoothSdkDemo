package com.worth.bluetooth.business.utils

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.worth.framework.base.core.utils.L
import java.lang.reflect.Method


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/14/21 --> 4:45 PM
 * Description: This is BluetoothUtil
 */
internal class BluetoothUtil private constructor() {
    private var connectGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    //蓝牙是否可用
    private var bleEnable = false

    /**
     * 检测设备是否支持蓝牙
     */
    internal fun checkBlueToothEnable(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return if (bluetoothAdapter == null) {
            L.d("该设备不支持蓝牙")
            false
        } else {
            L.d("该设备能支持蓝牙")
            true
        }
    }

    /**
     * 让用户去设置蓝牙
     */
    internal fun toSettingBluetooth(activity: Activity) {
        if (bleEnable) {
            val blueTooth = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            activity.startActivity(blueTooth)
        }
    }

    /**
     * 打开蓝牙
     */
    internal fun onBlueTooth() {
        if (bleEnable) {
            if (bluetoothAdapter!!.isEnabled) {
                L.d("蓝牙已打开，不用在点了~")
            } else {
                bluetoothAdapter!!.enable()
            }
        }
    }

    /**
     * 关闭蓝牙
     */
    internal fun offBlueTooth() {
        if (bleEnable) {
            if (bluetoothAdapter!!.isEnabled) {
                bluetoothAdapter!!.disable()
            } else {
                L.d("蓝牙已关闭，不用在点了~")
            }
        }
    }

    /**
     * 获取已经配对的设备
     */
    internal val connectedDevices: Set<BluetoothDevice>?
        get() {
            if (bleEnable) {
                bluetoothAdapter?.run {
                    if (isEnabled) {
                        return bondedDevices
                    }
                }
            }
            return null
        }

    /**
     * 可发现模式
     * 默认情况下，设备的可发现模式会持续120秒。
     * 通过给Intent对象添加EXTRA_DISCOVERABLE_DURATION附加字段，可以定义不同持续时间。目前设置300秒
     * 应用程序能够设置的最大持续时间是3600秒
     */
    internal fun discoverableDuration(activity: Activity) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        //定义持续时间
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        activity.startActivity(discoverableIntent)
    }

    /**
     * 扫描蓝牙，会走广播
     */
    internal fun startDiscovery() {
        if (bleEnable) {
            bluetoothAdapter?.run {
                if (!isDiscovering) {
                    startDiscovery()
                    L.d("扫描蓝牙设备")
                }
            }
        }
    }

    /**
     * 停止扫描
     */
    internal fun stopDiscovery() {
        if (bleEnable) {
            bluetoothAdapter?.run {
                if (isDiscovering) {
                    cancelDiscovery()
                    L.d("停止扫描蓝牙设备")
                }
            }
        }
    }

    /**
     * 扫描蓝牙
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    internal fun startScan() {
        if (bleEnable) {
            bluetoothAdapter!!.bluetoothLeScanner.startScan(object : ScanCallback() {
                override fun onScanResult(
                    callbackType: Int,
                    result: ScanResult
                ) {
                    //信号强度，是负的，数值越大代表信号强度越大
                    result.rssi
                    super.onScanResult(callbackType, result)
                }

                override fun onBatchScanResults(results: List<ScanResult>) {
                    super.onBatchScanResults(results)
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                }
            })
            L.d("扫描蓝牙设备")
        }
    }

    /**
     * 停止扫描
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    internal fun stopScan() {
        if (bleEnable) {
            bluetoothAdapter?.run {
                bluetoothLeScanner.stopScan(object : ScanCallback() {
                    override fun onScanResult(
                        callbackType: Int,
                        result: ScanResult
                    ) {
                        super.onScanResult(callbackType, result)
                    }

                    override fun onBatchScanResults(results: List<ScanResult>) {
                        super.onBatchScanResults(results)
                    }

                    override fun onScanFailed(errorCode: Int) {
                        super.onScanFailed(errorCode)
                    }
                })
            }
        }
    }

    /**
     * 连接设备
     */
    internal fun connect(context: Context, device: BluetoothDevice) {
        stopDiscovery()
        if (bleEnable) {
            connectGatt = device.connectGatt(context, true, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    when (status) {
                        BluetoothGatt.GATT_SUCCESS -> {
                        }
                        BluetoothProfile.STATE_CONNECTED -> {
                        }
                    }
                    super.onConnectionStateChange(gatt, status, newState)
                }
            })
        }
    }

    /**
     * 断开连接设备
     */
    internal fun disconnect() {
        stopDiscovery()
        if (bleEnable) {
            connectGatt?.run {
                disconnect()
                close()
                null
            }
        }
    }

    /**
     * 清理蓝牙缓存
     */
    internal fun refreshDeviceCache(): Boolean {
        if (connectGatt != null) {
            val tempVal: BluetoothGatt = connectGatt as BluetoothGatt
            try {
                val localMethod = tempVal.javaClass.getMethod(
                    "refresh", *arrayOfNulls(0)
                )
                if (localMethod != null) {
                    return (localMethod.invoke(
                        tempVal, *arrayOfNulls(0)
                    ) as Boolean)
                }
            } catch (localException: Exception) {
                L.i("refreshDeviceCache", "An exception occured while refreshing device")
            }
        }
        return false
    }

    init {
        bleEnable = checkBlueToothEnable()
    }

    /**
     * 对象单例
     */
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = BluetoothUtil()
    }
}
