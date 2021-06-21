package com.worth.bluetooth.business.utils

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.worth.bluetooth.business.utils.PadSdkExt.BlueToothConnectReceiver.OnBleConnectListener
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
    private var blueToothBondReceiver: BlueToothBondReceiver? = null

    fun register(application: Application?) {
        application?.let {
            registerBlueConnectionReceiver(it)
            registerBluetoothStateReceiver(it)
            registerBlueToothBondReceiver(it)
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        application?.let {
            unregisterBlueConnectionReceiver(it)
            unregisterBluetoothStateReceiver(it)
            unRegisterBlueToothBondReceiver(it)
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

    private fun registerBlueToothBondReceiver(application: Application) {
        //蓝牙连接广播
        blueToothBondReceiver = BlueToothBondReceiver()
        val connect = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        application.registerReceiver(blueToothConnectReceiver, connect)
    }

    private fun unRegisterBlueToothBondReceiver(application: Application) {
        application?.run {
            if (blueToothBondReceiver != null) {
                unregisterReceiver(blueToothBondReceiver)
            }
        }
    }

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
                        Log.e("BluetoothUtils","正在配对")
                    BluetoothDevice.BOND_BONDED ->                      //配对结束
                        Log.e("BluetoothUtils","配对结束")
                    BluetoothDevice.BOND_NONE ->                        //取消配对/未配对
                        Log.e("BluetoothUtils","取消配对/未配对")
                    else -> {
                        Log.e("BluetoothUtils","配对其他情况")
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
                Log.e("BluetoothUtils","蓝牙已链接-收到回调-：" + device?.name)

            }

            override fun onDisConnect(device: BluetoothDevice?) {
                Log.e("BluetoothUtils","蓝牙已断开-收到回调-：" + device?.name)
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
                    Log.e("BluetoothUtils","蓝牙已连接：" + device?.name)
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    if (onBleConnectListener != null) {
                        onBleConnectListener?.onDisConnect(device)
                    }
                    Log.e("BluetoothUtils","蓝牙已断开：" + device?.name)
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
                    BluetoothAdapter.STATE_OFF -> Log.e("BluetoothUtils","蓝牙已关闭")
                    BluetoothAdapter.STATE_ON -> Log.e("BluetoothUtils","蓝牙已打开")
                    BluetoothAdapter.STATE_TURNING_ON -> Log.e("BluetoothUtils","正在打开蓝牙")
                    BluetoothAdapter.STATE_TURNING_OFF -> Log.e("BluetoothUtils","正在关闭蓝牙")
                    else -> Log.e("BluetoothUtils","未知状态")
                }
            }
        }
    }

    /**********************************************************************************************/

}

const val DEFAULT_VALUE_BLUETOOTH = 1000

