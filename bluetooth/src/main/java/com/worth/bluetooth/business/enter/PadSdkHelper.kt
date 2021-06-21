package com.worth.bluetooth.business.enter

import android.app.Activity
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.clj.fastble.BleManager
import com.clj.fastble.callback.*
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.clj.fastble.utils.HexUtil
import com.worth.bluetooth.business.ext.setConnMacId
import com.worth.bluetooth.business.ext.setMacId
import com.worth.bluetooth.business.gloable.*
import com.worth.bluetooth.business.utils.FastSendIntercept
import com.worth.bluetooth.business.utils.PadSdkGlobalHandler
import com.worth.bluetooth.business.utils.ParseHelper
import com.worth.framework.base.core.storage.MeKV
import com.worth.framework.base.core.utils.LDBus
import com.worth.framework.base.core.utils.application


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/25/21 --> 4:45 PM
 * Description: This is PadSdkHelper
 */
class PadSdkHelper private constructor() {
    private val TAG = "PadSdkHelper"
    private val isAutoConnect: Boolean = false  //  是否自动重连
    private val context = application
    private var scanResultListTemp: MutableList<BleDevice> = mutableListOf()
    private var mScanTimeOut: Long = 3456L

    private var conn = false
    private var currGatt: BluetoothGatt? = null

    /**
     * 初始化sdk
     * @param reConnectCount            重连次数
     * @param reConnectCountInterval    重连时间间隔
     * @param connectOverTime           链接超时时间
     */
    @JvmOverloads
    fun initPadSdk(
        reConnectCount: Int = 1,
        reConnectCountInterval: Long = 5000,
        connectOverTime: Long = 20000
    ): PadSdkHelper {
        BleManager.getInstance().init(context)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(reConnectCount, reConnectCountInterval)
            .setConnectOverTime(connectOverTime)
            .operateTimeout = 5000
        return this
    }

    /**
     * 配置扫描规则
     */
    private fun initScanRule(scanTimeOut: Long, vararg bluetoothName: String) {
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setServiceUuids(null)                                              //  只扫描指定的服务的设备，可选
            .setDeviceName(true, *bluetoothName)                         //  只扫描指定广播名的设备，可选
            .setDeviceMac(null)                                                 //  只扫描指定mac的设备，可选
            .setAutoConnect(isAutoConnect)                                      //  连接时的autoConnect参数，可选，默认false
            .setScanTimeOut(scanTimeOut)                                        //  扫描超时时间，可选，默认10秒
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)
    }

    /**
     * 搜索设备
     * 可以获取到蓝牙的名称和物理地址，在未连接之前，拿不到uuid。
     * @param bluetoothName 过滤蓝牙的前缀名称
     */
    @JvmOverloads
    fun scanDevices(
        scanTimeOut: Long = 5000,
        vararg bluetoothName: String = arrayOf("proximity", "iMEMBER")
    ) {
        if (mScanTimeOut != scanTimeOut) {
            mScanTimeOut = scanTimeOut
            initScanRule(scanTimeOut, *bluetoothName)
        }
        BleManager.getInstance().scan(bleScanCallback)
    }

    private val bleScanCallback = object : BleScanCallback() {
        override fun onScanStarted(success: Boolean) {
            LDBus.sendSpecial2(EVENT_TO_APP, START_SCAN, "")
        }

        override fun onScanning(bleDevice: BleDevice?) {
            bleDevice?.run {
                LDBus.sendSpecial2(EVENT_TO_APP, SCANNING, this)
            }
        }

        override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
            scanResultList?.run {
                checkDeviceList(this)
                if (scanResultListTemp.isNotEmpty() && scanResultListTemp == scanResultList) return
                LDBus.sendSpecial2(EVENT_TO_APP, SCAN_FINISH, this)
            } ?: run {
                LDBus.sendSpecial2(
                    EVENT_TO_APP,
                    SCAN_FINISH,
                    mutableListOf<BleDevice>()
                )
            }

            if (!conn) {
                scanDevices()
            }
        }
    }

    /**
     * 设置macId
     */
    internal fun setMacId(macId: String?): PadSdkHelper {
        macId?.let { MeKV.setMacId(it) }
        return this
    }

    /**
     * 连接设备
     */
    fun connect(bleDevice: BleDevice) {
        //  检查设备连接,用户手动点击某个wifi进行连接
        val result = ParseHelper.instance.parseRecord(bleDevice.scanRecord)
        result?.run {
            when {
                startsWith(UNPAIRED) -> {
                    connectionAndNotify(bleDevice, false)
                }
                else -> {
                    Log.e(TAG, "checkDevice--其他广播")
                }
            }
        }
    }

    /**
     * 连接和通知
     * @param isPaired 已经成功配对过了
     */
    private fun connectionAndNotify(bd: BleDevice, isPaired: Boolean) {
        if (!BleManager.getInstance().isConnected(bd)) {
            BleManager.getInstance().connect(bd, object : BleGattCallback() {
                override fun onStartConnect() {
                    conn = false
                    LDBus.sendSpecial2(EVENT_TO_APP, START_CONN, bd)
                }

                override fun onConnectFail(bd: BleDevice, ex: BleException) {
                    conn = false
                    LDBus.sendSpecial2(EVENT_TO_APP, CONN_FAIL, ex)
                }

                override fun onConnectSuccess(bd: BleDevice, gatt: BluetoothGatt, status: Int) {
                    conn = true
                    currGatt = gatt
                    LDBus.sendSpecial2(EVENT_TO_APP, CONN_OK, gatt)
                    val service = ParseHelper.instance.findService(gatt)
                    val character = ParseHelper.instance.findCharacteristic(service)
                    service?.run {
                        //  接收配对通知-一次建立连接之后 在notify中进行后续的数据处理
                        notify(bd, uuid.toString(), character?.uuid.toString(),
                            object : BleNotifyCallback() {
                                override fun onNotifySuccess() {}
                                override fun onNotifyFailure(exception: BleException?) {}
                                override fun onCharacteristicChanged(data: ByteArray?) {
                                    handleNotify(data, bd, character)
                                }
                            }
                        )

                        if (!isPaired) {  //  已经配对过了 不需要再次校验data
                            write(bd, uuid.toString(), character?.uuid.toString(), checkData)
                        }
                    }
                }

                override fun onDisConnected(
                    disC: Boolean,
                    bd: BleDevice,
                    gatt: BluetoothGatt,
                    status: Int
                ) {
                    conn = false
                    Log.e(TAG, "当前设备已断开连接")
                    scanDevices()
                    LDBus.sendSpecial2(EVENT_TO_APP, DIS_CONN, gatt)
                }
            })
        } else {
            Log.e(TAG, "当前设备已连接-不进行逻辑处理")
        }
    }

    /**
     * 扫描结果发现是长按的事件且未连接该设备
     */
    private fun checkDeviceList(devices: List<BleDevice>) {
        devices?.forEach { bleDevice ->
            if (!BleManager.getInstance().isConnected(bleDevice)) {
                val result = ParseHelper.instance.parseRecord(bleDevice.scanRecord)
                result?.run {
                    when {
                        startsWith(AFTER_PAIRED) -> {
                            connectionAndNotify(bleDevice, true)
                            Log.e(TAG, "配对成功，扫描到该设备的广播，目前进行直接连接处理")
                        }
                        startsWith(LONG_PRESS) -> {
                            Log.e(TAG, "长按10秒配对的广播")
                            connectionAndNotify(bleDevice, false)
                        }
                        startsWith(DOUBLE_CLICK_CONN4)
                                || startsWith(DOUBLE_CLICK_DIS_CONN5)
                                || startsWith(DOUBLE_CLICK_DIS_CONN7) -> {
                            if (!FastSendIntercept.doubleSendIntercept()) {
                                LDBus.sendSpecial2(
                                    EVENT_TO_APP,
                                    DOUBLE_CLICK,
                                    bleDevice
                                )
                                Log.e(TAG, "有效事件---->已连接时候——双击的广播-1")
                            } else {
                                Log.e(TAG, "20秒内收到重复双击广播信号，只处理一次服务请求")
                            }
                        }
                        else -> {
                            Log.e(TAG, "未配对过，需要手动点击配对--app端拿到list之后进行操作")
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理notify的结果
     */
    private fun BluetoothGattService.handleNotify(
        data: ByteArray?,
        bleDevice: BleDevice,
        character: BluetoothGattCharacteristic?
    ) {
        var result = HexUtil.formatHexString(data)
        Log.e("handleNotify-result", result)
        if (result.startsWith(RESULT_FIRST)) {
            result = result.substring(2)
            when {
                result.startsWith(
                    RESULT_DATA_TYPE_OK_FAIL_TIME_OUT
                ) -> {              //  成功失败、or超时
                    result = result.substring(4)
                    when {
                        result.startsWith(RESULT_DATA_FAIL) -> {
                            LDBus.sendSpecial2(EVENT_TO_APP, PAIR_FAIL, bleDevice)
                            Log.e(TAG, "配对错误")
                        }
                        result.startsWith(RESULT_DATA_OK) -> {
                            LDBus.sendSpecial2(EVENT_TO_APP, PAIR_OK, bleDevice)
                            Log.e(TAG, "配对成功")
                        }
                        result.startsWith(RESULT_DATA_TIME_OUT) -> {
                            LDBus.sendSpecial2(EVENT_TO_APP, PAIR_TIME_OUT, bleDevice)
                            write(
                                bleDevice,
                                uuid.toString(),
                                character?.uuid.toString(),
                                failToDeviceFlash
                            )
                            Log.e(TAG, "配对超时")
                        }
                    }
                }
                result.startsWith(
                    RESULT_DATA_TYPE_MATH_RESULT
                ) -> {        //  返回了运算结果
                    result = result.substring(4)
                    result?.run {
                        val resultInt = result.toInt()
                        if (resultInt == 9) {
                            Log.e(TAG, "请求macId")
                            write(
                                bleDevice,
                                uuid.toString(),
                                character?.uuid.toString(),
                                resultOk
                            )
                        } else {
                            Log.e(TAG, "校验失败")
                            write(
                                bleDevice,
                                uuid.toString(),
                                character?.uuid.toString(),
                                resultFail
                            )
                        }
                    }
                }
                result.startsWith(
                    RESULT_DATA_TYPE_MAC_ADDRESS
                ) -> {
                    result = result.substring(4)
                    //  返回了mac地址
                    Log.e(TAG, "设备返回的mac地址:$result")
                    MeKV.setConnMacId(result)
                    write(
                        bleDevice,
                        uuid.toString(),
                        character?.uuid.toString(),
                        successToDeviceFlash
                    )
                }
                result.startsWith(RESULT_DATA_TYPE_CLICK) -> {
                    //  返回了单击的事件
                    Log.e(TAG, "设备触发了单击的事件")
                    LDBus.sendSpecial2(EVENT_TO_APP, CLICK, bleDevice)
                    write(
                        bleDevice,
                        uuid.toString(),
                        character?.uuid.toString(),
                        toDeviceClickResult
                    )
                }
            }
        }
    }

    /**
     * 取消扫描
     * 如果调用该方法，如果还在扫描状态，则立即结束，并回调该onScanFinished方法。
     */
    fun cancelScan() {
        BleManager.getInstance().cancelScan()
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
    private fun disconnectAllDevice() {
        BleManager.getInstance().disconnectAllDevice()
    }


    /**
     *  在不扩大MTU和扩大MTU的无效性的情况下，发送超过20字节的长数据时需要分包。该参数boolean split表示是否使用报文传递；
     *  write不带boolean split参数的方法默认将数据分包20多个字节。
     *  在onWriteSuccess回调方法上：current表示当前发送的包数，total表示本次的总包数据，justWrite表示刚刚发送成功的包。
     */
    fun write(bd: BleDevice, uuidS: String, uuidW: String, d: ByteArray) {
        PadSdkGlobalHandler.ins().mHandler.get()?.postDelayed({
            BleManager.getInstance()
                .write(bd, uuidS, uuidW, d, true, object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        LDBus.sendSpecial2(EVENT_TO_APP, WRITE_OK, "")
                        Log.e(TAG, "写入数据到设备成功")
                    }

                    override fun onWriteFailure(exception: BleException?) {
                        LDBus.sendSpecial2(EVENT_TO_APP, WRITE_FAIL, "")
                        Log.e(TAG, "写入数据到设备失败")
                    }
                })
        }, 500)
    }

    /**
     * 打开 or 关闭蓝牙
     */
    fun onOrOffBlueTooth(open: Boolean) {
        if (checkBlueToothEnable()) {
            if (open) {
                BleManager.getInstance().enableBluetooth()
            } else {
                BleManager.getInstance().disableBluetooth()
            }
        }
    }

    /**
     * 去设置页面打开蓝牙操作
     */
    fun toSettingBluetooth(activity: Activity) {
        if (checkBlueToothEnable()) {
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
    val connectedDevices: List<BleDevice>?
        get() = BleManager.getInstance().allConnectedDevice


    @JvmOverloads
    private fun notify(bd: BleDevice, uuidS: String, uuidN: String, callback: BleNotifyCallback) {
        BleManager.getInstance().notify(bd, uuidS, uuidN, callback)
    }

    fun stopNotify(bd: BleDevice, uuidS: String, uuidN: String) {
        BleManager.getInstance().stopNotify(bd, uuidS, uuidN)
    }

    /**
     * 控制led灯闪烁
     * @param count 要设置闪烁的次数
     * @param intervalTime  要设置闪烁每次的时间  毫秒级 比如1000毫秒
     */
    fun setFlashInfo(bleDevice: BleDevice, count: Int = 3, intervalTime: Int = 1000) {
        val resultData = ParseHelper.instance.setFlashInfo(count, intervalTime)
        currGatt?.let { gatt ->
            val service = ParseHelper.instance.findService(gatt)
            service?.let { service ->
                val character = ParseHelper.instance.findCharacteristic(service)
                character?.let {
                    write(
                        bleDevice,
                        service.uuid.toString(),
                        character?.uuid.toString(),
                        resultData
                    )
                }
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        BleManager.getInstance().cancelScan()
        disconnectAllDevice()
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
