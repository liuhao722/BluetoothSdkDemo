package com.worth.bluetooth.business.callbacks

import android.bluetooth.BluetoothDevice

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/15/21 --> 12:55 AM
 * Description: This is SearchCallback
 */
interface SearchCallback {
    /**
     * 在进行搜索前回调
     */
    fun onStartDiscovery()

    /**
     * 当寻找到一个新设备时回调
     */
    fun onNewDeviceFound(device: BluetoothDevice)

    /**
     * 当搜索蓝牙设备完成后回调
     */
    fun onSearchCompleted(
        bondedList: List<BluetoothDevice>,
        newList: List<BluetoothDevice>
    )

    /**
     * 当搜索蓝牙设备完成后回调
     */
    fun onError(e: java.lang.Exception)
}