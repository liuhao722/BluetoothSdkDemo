package com.worth.bluetooth.business.utils

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.worth.bluetooth.business.callbacks.SearchCallback
import com.worth.bluetooth.business.utils.PadSdkExt.BlueToothConnectReceiver.OnBleConnectListener
import com.worth.bluetooth.other.BtHelperClient
import com.worth.bluetooth.other.MessageItem
import com.worth.bluetooth.other.OnSearchDeviceListener
import com.worth.bluetooth.other.OnSendMessageListener
import com.worth.framework.base.core.utils.L
import com.worth.framework.base.core.utils.application


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/14/21 --> 4:45 PM
 * Description: This is BluetoothUtils
 */
internal class PadSdkExt private constructor() {
    private val TAG = "BluetoothUtils"
    private var blueToothConnectReceiver: BlueToothConnectReceiver? = null
    private var blueToothValueReceiver: BlueToothValueReceiver? = null
    var btHelperClient: BtHelperClient? = null

    fun initSdk() {
        if (btHelperClient != null) return
        btHelperClient = BtHelperClient.from(application)
        application?.let {
            registerBlueConnectionReceiver(it)
            registerBluetoothStateReceiver(it)
        }
    }

    /**
     * 搜索设备
     * 可以获取到蓝牙的名称和物理地址，在未连接之前，拿不到uuid。
     */
    internal fun searchDevices(callback: SearchCallback) {
        btHelperClient?.searchDevices(object : OnSearchDeviceListener {
            override fun onStartDiscovery() {
                callback?.onStartDiscovery()
                // 在进行搜索前回调
                L.d(TAG, "onStartDiscovery()")
            }

            @SuppressLint("MissingPermission")
            override fun onNewDeviceFound(device: BluetoothDevice) {
                callback?.onNewDeviceFound(device)
                // 当寻找到一个新设备时回调
                L.d(TAG, "device: ${device.name} ${device.address}")
            }

            override fun onSearchCompleted(
                bondedList: List<BluetoothDevice>,
                newList: List<BluetoothDevice>
            ) {
                callback?.onSearchCompleted(bondedList, newList)
                // 当搜索蓝牙设备完成后回调
                L.d(TAG, "SearchCompleted: bondedList$bondedList")
                L.d(TAG, "SearchCompleted: newList$newList")
            }

            override fun onError(e: java.lang.Exception) {
                callback?.onError(e)
                e.printStackTrace()
            }
        })
    }

    /**
     * @param macId mac地址 "20:15:03:18:08:63"
     * @param msg 要发送的内容
     */
    internal fun sendMsg(macId: String, msg: String) {
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
    internal fun filter() {
        btHelperClient?.setFilter { response -> response.trim { it <= ' ' }.length >= 5 }
    }

    /**
     * 释放资源
     */
    internal fun release() {
        btHelperClient?.close()
        application?.let {
            unregisterBlueConnectionReceiver(it)
            unregisterBluetoothStateReceiver(it)
        }
    }

    /**
     * 对象单例
     */
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = PadSdkExt()
    }

    /**********************************************************************************************/


    /**
     * 蓝牙配对广播
     */
    internal class BlueToothBondReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                // 找到设备后获取其设备
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                when (device?.bondState) {
                    BluetoothDevice.BOND_BONDING ->                     //正在配对
                        L.d("正在配对")
                    BluetoothDevice.BOND_BONDED ->                      //配对结束
                        L.d("配对结束")
                    BluetoothDevice.BOND_NONE ->                        //取消配对/未配对
                        L.d("取消配对/未配对")
                    else -> {
                        L.d("配对其他情况")
                    }
                }
            }
        }
    }

    /**********************************************************************************************/


    private fun registerBlueConnectionReceiver(application: Application) {

        //蓝牙连接广播
        blueToothConnectReceiver = BlueToothConnectReceiver()
        val connect = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        application.registerReceiver(blueToothConnectReceiver, connect)
        val disconnect = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        application.registerReceiver(blueToothConnectReceiver, disconnect)
        blueToothConnectReceiver?.setOnBleConnectListener(object : OnBleConnectListener {
            override fun onConnect(device: BluetoothDevice?) {
                L.d("蓝牙已链接-收到回调-setOnBleConnectListener：" + device?.name)

            }

            override fun onDisConnect(device: BluetoothDevice?) {
                L.d("蓝牙已断开-收到回调-setOnBleConnectListener：" + device?.name)
            }
        })
    }


    private fun unregisterBlueConnectionReceiver(application: Application) {
        blueToothConnectReceiver?.let {
            application.unregisterReceiver(it)
        }
    }

    /**
     * 蓝牙连接广播
     */
    internal class BlueToothConnectReceiver : BroadcastReceiver() {
        private var onBleConnectListener: OnBleConnectListener? = null
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    if (onBleConnectListener != null) {
                        onBleConnectListener?.onConnect(device)
                    }
                    L.d("蓝牙已连接：" + device?.name)
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    if (onBleConnectListener != null) {
                        onBleConnectListener?.onDisConnect(device)
                    }
                    L.d("蓝牙已断开：" + device?.name)
                }
            }
        }

        internal interface OnBleConnectListener {
            fun onConnect(device: BluetoothDevice?)
            fun onDisConnect(device: BluetoothDevice?)
        }

        internal fun setOnBleConnectListener(onBleConnectListener: OnBleConnectListener?) {
            this.onBleConnectListener = onBleConnectListener
        }
    }


    /**********************************************************************************************/

    private fun registerBluetoothStateReceiver(application: Application) {
        //注册广播，蓝牙状态监听
        if (blueToothValueReceiver == null) {
            blueToothValueReceiver = BlueToothValueReceiver()
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            application.registerReceiver(blueToothValueReceiver, filter)
        }
    }

    private fun unregisterBluetoothStateReceiver(application: Application) {
        blueToothValueReceiver?.let {
            application.unregisterReceiver(it)
        }
    }

    /**
     * 广播监听蓝牙状态
     */
    internal class BlueToothValueReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, DEFAULT_VALUE_BLUETOOTH)) {
                    BluetoothAdapter.STATE_OFF -> L.d("蓝牙已关闭")
                    BluetoothAdapter.STATE_ON -> L.d("蓝牙已打开")
                    BluetoothAdapter.STATE_TURNING_ON -> L.d("正在打开蓝牙")
                    BluetoothAdapter.STATE_TURNING_OFF -> L.d("正在关闭蓝牙")
                    else -> L.d("未知状态")
                }
            }
        }
    }

    /**********************************************************************************************/

}

const val DEFAULT_VALUE_BLUETOOTH = 1000

