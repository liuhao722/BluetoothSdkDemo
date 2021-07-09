package com.worth.bluetooth.business.enter

import android.app.Activity
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.clj.fastble.utils.HexUtil
import com.espressif.iot.esptouch.EspHelper
import com.worth.bluetooth.business.ext.setConnMacId
import com.worth.bluetooth.business.ext.setMacId
import com.worth.bluetooth.business.gloable.*
import com.worth.bluetooth.business.utils.PadSdkGlobalHandler
import com.worth.bluetooth.business.utils.ParseHelper
import com.worth.framework.base.core.storage.MeKV
import com.worth.framework.base.core.utils.LDBus
import com.worth.framework.base.core.utils.application
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/25/21 --> 4:45 PM
 * Description: This is PadSdkHelper
 */
class PadSdkHelper private constructor() {
    private val TAG = "PadSdkHelper"
    private val context = application
    private var scanResultListTemp: List<BleDevice> = arrayListOf()
    private var mScanTimeOut: Long = 3456L          //  扫描超时时间

    private var currGatt: BluetoothGatt? = null     //  当前的蓝牙特征
    private var isClickedScanBtn = false            //  是否手动触发扫描过
    private var isDebugConfig: Boolean = false
    private var isPhoneType: Boolean = true
    private val defScanTime = 3000L                 //  默认扫描一次蓝牙设备的间隔时间

    /**
     * 初始化sdk
     * @param reConnectCount            重连次数
     * @param reConnectCountInterval    重连时间间隔
     * @param stopScanIntervalTime      扫描一次到下一次的时间间隔--单蓝牙设备的  默认30秒一次
     * @param connectOverTime           链接超时时间
     */
    @JvmOverloads
    fun initPadSdk(
        reConnectCount: Int = 1,
        reConnectCountInterval: Long = 5_000,
        stopScanIntervalTime: Long = 30_000,
        connectOverTime: Long = 20_000,
        isPhone: Boolean = true
    ): PadSdkHelper {
        isPhoneType = isPhone
        context?.let {
            BleManager.getInstance().init(it)
            EspHelper.initSdk(it)
        }
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(reConnectCount, reConnectCountInterval)
            .setConnectOverTime(connectOverTime)
            .operateTimeout = 5_000
        initStopScanIntervalTime(stopScanIntervalTime)
        return this
    }

    /**
     * 搜索设备
     * 可以获取到蓝牙的名称和物理地址，在未连接之前，拿不到uuid。
     * @param setIsDebug 是否是调试模式，如果是调试模式全部进行展示列表数据
     * @param bluetoothName 过滤蓝牙的前缀名称 会员卡："proximity", "iMEMBER"  基站："iStation"
     */
    @JvmOverloads
    fun scanDevices(
        setIsDebug: Boolean = false,
        scanTimeOut: Long = defScanTime,
        vararg bluetoothName: String = arrayOf("proximity", "iMEMBER", "iStation")
    ) {
        isDebugConfig = setIsDebug
        isClickedScanBtn = true
        mScanTimeOut = scanTimeOut
        initScanRule(scanTimeOut, *bluetoothName)
        scan()
    }

    internal fun scan() {
        BleManager.getInstance().scan(bleScanCallback)
    }

    /**
     * 连接设备-检查设备连接,用户手动点击某个wifi进行连接
     */
    fun connect(bleDevice: BleDevice) {
        var result = ParseHelper.instance.parseRecord(bleDevice.scanRecord)
        result?.let {
            result = it.substring(0, 2)
            val content = it.subSequence(2, it.length)
            result?.let { type ->
                when {
                    type.startsWith(
                        I_STATION,
                        true
                    ) -> {                               //  基站广播-返回的设备列表不会显示基站信息，暂时留此处做备用

                    }
                    type.startsWith(VIP_CARD, true) -> {                                //  vip卡广播
                        content?.let { state ->
                            when {
                                state.startsWith(AFTER_PAIRED) -> {
                                    connectionAndNotify(bleDevice, true)
                                }
                                else -> {
                                    connectionAndNotify(bleDevice, false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 控制led灯闪烁
     * @param count 要设置闪烁的次数
     * @param intervalTime  要设置闪烁每次的时间  毫秒级 比如1000毫秒
     */
    fun controlLed(bd: BleDevice, count: Int = 3, intervalTime: Int = 1000) {
        val resultData = ParseHelper.instance.setFlashInfo(count, intervalTime)
        currGatt?.let { gatt ->
            val service = ParseHelper.instance.findService(gatt)
            service?.let { service ->
                val character = ParseHelper.instance.findCharacteristic(service)
                character?.let {
                    write(bd, service.uuid, character?.uuid, resultData)
                }
            }
        }
    }

    /**
     * 写入到设备中
     */
    fun writeToDevices(bd: BleDevice, data: ByteArray) {
//        判断设备
//        bd.device.address
//        currGatt?.device?.address
        currGatt?.let { gatt ->
            val service = ParseHelper.instance.findService(gatt)
            service?.let { service ->
                val character = ParseHelper.instance.findCharacteristic(service)
                character?.let {
                    write(bd, service.uuid, character?.uuid, data)
                }
            }
        }
    }

    /**
     *  在不扩大MTU和扩大MTU的无效性的情况下，发送超过20字节的长数据时需要分包。该参数boolean split表示是否使用报文传递；
     *  write不带boolean split参数的方法默认将数据分包20多个字节。
     *  在onWriteSuccess回调方法上：current表示当前发送的包数，total表示本次的总包数据，justWrite表示刚刚发送成功的包。
     */
    internal fun write(bd: BleDevice, uuidS: UUID, uuidW: UUID, data: ByteArray) {
        PadSdkGlobalHandler.ins().mHandler.get()?.postDelayed({
            BleManager.getInstance()
                .write(
                    bd,
                    uuidS.toString(),
                    uuidW.toString(),
                    data,
                    true,
                    object : BleWriteCallback() {
                        override fun onWriteSuccess(curr: Int, total: Int, data: ByteArray?) {
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
     * 获取已经配对的设备
     */
    internal val connectedDevices: List<BleDevice>?
        get() = BleManager.getInstance().allConnectedDevice

    /**
     * 取消扫描--但不能真正的停止 还需要扫描基站的广播！！！哎！！！
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
     * 释放资源
     */
    fun release() {
        if (isClickedScanBtn) {
            cancelScan()
        }
        disconnectAllDevice()
        BleManager.getInstance().destroy()
        EspHelper.release()
    }

    /**
     * 配置扫描规则
     */
    private fun initScanRule(scanTimeOut: Long, vararg bluetoothName: String) {
        if (isDebugConfig) {                                                                        //  全部扫描
            val scanRuleConfig = BleScanRuleConfig.Builder()
                .setServiceUuids(null)
                .setDeviceMac(null)                                                                 //  只扫描指定mac的设备，可选
                .setAutoConnect(false)                                                              //  连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(scanTimeOut)                                                        //  扫描超时时间，可选，默认10秒
                .build()
            BleManager.getInstance().initScanRule(scanRuleConfig)
        } else {                                                                                    //  只扫描指定的服务的设备，可选
            val scanRuleConfig = BleScanRuleConfig.Builder()
                .setDeviceName(
                    true,
                    *bluetoothName
                )                                         //  只扫描指定广播名的设备，可选
                .setServiceUuids(null)
                .setDeviceMac(null)                                                                 //  只扫描指定mac的设备，可选
                .setAutoConnect(false)                                                              //  连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(scanTimeOut)                                                        //  扫描超时时间，可选，默认10秒
                .build()
            BleManager.getInstance().initScanRule(scanRuleConfig)
        }
    }

    /**
     * 连接和通知
     * @param isPaired 已经成功配对过了
     */
    private fun connectionAndNotify(bd: BleDevice, isPaired: Boolean) {
        if (BleManager.getInstance().isConnected(bd)) return
        BleManager.getInstance().connect(bd, object : BleGattCallback() {
            override fun onStartConnect() {
                cancelScan()
                LDBus.sendSpecial2(EVENT_TO_APP, START_CONN, bd)
            }

            override fun onConnectFail(bd: BleDevice, ex: BleException) {
                LDBus.sendSpecial2(EVENT_TO_APP, CONN_FAIL, ex)
            }

            override fun onConnectSuccess(bd: BleDevice, gatt: BluetoothGatt, status: Int) {
                cancelScan()
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
                        character?.uuid?.let { write(bd, uuid, it, checkData) }
                    }
                }
            }

            override fun onDisConnected(disC: Boolean, b: BleDevice, g: BluetoothGatt, s: Int) {
                Log.e(TAG, "当前设备已断开连接")
                LDBus.sendSpecial2(EVENT_TO_APP, DIS_CONN, g)
            }
        })
    }

    private val bleScanCallback = object : BleScanCallback() {
        override fun onScanStarted(success: Boolean) {
            LDBus.sendSpecial2(EVENT_TO_APP, START_SCAN, "")
        }

        override fun onScanning(bleDevice: BleDevice?) {
            ParseHelper.instance.checkDevice(bleDevice)?.run {
                LDBus.sendSpecial2(EVENT_TO_APP, SCANNING, this)
            }
        }

        override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
            scanResultList?.run {
                val result = ParseHelper.instance.checkDeviceList(this)
                if (scanResultListTemp.isNotEmpty() && scanResultListTemp == scanResultList) return
                scanResultListTemp = result
                LDBus.sendSpecial2(EVENT_TO_APP, SCAN_FINISH, result)
            } ?: run {
                LDBus.sendSpecial2(EVENT_TO_APP, SCAN_FINISH, mutableListOf<BleDevice>())
            }
            if (!isPhoneType) {     //  pad模式的时候，会一直扫描，但中间不会有数据的传输 只是做扫描处理
                scan()
            }
        }
    }

    /**
     * 处理notify的结果
     */
    private fun BluetoothGattService.handleNotify(
        data: ByteArray?,
        bd: BleDevice,
        character: BluetoothGattCharacteristic?
    ) {
        var result = HexUtil.formatHexString(data)
        Log.e("handleNotify-result", result)
        if (result.startsWith(RESULT_FIRST)) {
            result = result.substring(2)
            when {
                result.startsWith(RESULT_DATA_TYPE_1) -> {                                          //  成功失败、or超时
                    result = result.substring(4)
                    when {
                        result.startsWith(RESULT_DATA_FAIL) -> {
                            LDBus.sendSpecial2(EVENT_TO_APP, PAIR_FAIL, bd)
                            Log.e(TAG, "配对错误")
                        }
                        result.startsWith(RESULT_DATA_OK) -> {
                            LDBus.sendSpecial2(EVENT_TO_APP, PAIR_OK, bd)
                            Log.e(TAG, "配对成功")
                        }
                        result.startsWith(RESULT_DATA_TIME_OUT) -> {
                            LDBus.sendSpecial2(EVENT_TO_APP, PAIR_TIME_OUT, bd)
                            character?.uuid?.let { write(bd, uuid, it, failToDeviceFlash) }
                            Log.e(TAG, "配对超时")
                        }
                    }
                }
                result.startsWith(RESULT_DATA_TYPE_2) -> {                                          //  返回了运算结果
                    result = result.substring(4)
                    result?.run {
                        val resultInt = result.toInt()
                        if (resultInt == 9) {
                            Log.e(TAG, "请求macId")
                            character?.uuid?.let { write(bd, uuid, it, resultOk) }
                        } else {
                            Log.e(TAG, "校验失败")
                            character?.uuid?.let { write(bd, uuid, it, resultFail) }
                        }
                    }
                }
                result.startsWith(RESULT_DATA_TYPE_3) -> {                                          //  返回了mac地址
                    result = result.substring(4)
                    Log.e(TAG, "设备返回的mac地址:$result")
                    MeKV.setConnMacId(result)
                    character?.uuid?.let { write(bd, uuid, it, successToDeviceFlash) }
                }
                result.startsWith(RESULT_DATA_TYPE_4) -> {
                    //  返回了单击的事件
                    Log.e(TAG, "设备触发了单击的事件")
                    character?.uuid?.let { write(bd, uuid, it, toDeviceClickResult) }
                    LDBus.sendSpecial2(EVENT_TO_APP, CLICK, bd)
                }
            }
        }
    }

    /**
     * 设置macId
     */
    private fun setMacId(macId: String?): PadSdkHelper {
        macId?.let { MeKV.setMacId(it) }
        return this
    }

    private fun notify(bd: BleDevice, uuidS: String, uuidN: String, callback: BleNotifyCallback) {
        BleManager.getInstance().notify(bd, uuidS, uuidN, callback)
    }

    private fun stopNotify(bd: BleDevice, uuidS: String, uuidN: String) {
        BleManager.getInstance().stopNotify(bd, uuidS, uuidN)
    }

    /**
     * 蓝牙是否可用
     */
    private fun checkBlueToothEnable(): Boolean = BleManager.getInstance().isSupportBle

    /**
     * 断开所有已连接设备
     */
    private fun disconnectAllDevice() {
        BleManager.getInstance().disconnectAllDevice()
    }

    /**
     * 去设置页面打开蓝牙操作
     */
    private fun toSettingBluetoothView(activity: Activity) {
        if (checkBlueToothEnable()) {
            val blueTooth = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            activity.startActivity(blueTooth)
        }
    }

    /**
     * @param stopScanIntervalTime 扫描间隔时间，单位毫秒
     */
    private fun initStopScanIntervalTime(stopScanIntervalTime: Long) {
        Observable.interval(stopScanIntervalTime, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                scan()
            }
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
