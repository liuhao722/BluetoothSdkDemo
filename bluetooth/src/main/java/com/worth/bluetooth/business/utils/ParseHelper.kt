package com.worth.bluetooth.business.utils

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import com.clj.fastble.data.BleDevice
import com.clj.fastble.utils.HexUtil
import com.worth.bluetooth.business.gloable.*
import com.worth.framework.base.core.utils.L
import com.worth.framework.base.core.utils.LDBus


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/25/21 --> 4:45 PM
 * Description: This is ParseHelper 解析类
 */
class ParseHelper private constructor() {
    private val TAG = "ParseHelper"

    /**
     * 解析对应的状态，进行设备信息的确定
     */
    fun parseRecord(scanRecord: ByteArray): String? {
        val temp = ByteArray(14)
        temp[0] = scanRecord[5]             //  包头信息-区分是蓝牙还是基站

        temp[1] = scanRecord[6]             //  状态信息
        temp[2] = scanRecord[7]             //  状态信息

        temp[3] = scanRecord[8]             //  产品id
        temp[4] = scanRecord[9]             //  产品id

        temp[5] = scanRecord[10]            //  mac地址
        temp[6] = scanRecord[11]            //  mac地址
        temp[7] = scanRecord[12]            //  mac地址
        temp[8] = scanRecord[13]            //  mac地址
        temp[9] = scanRecord[14]            //  mac地址
        temp[10] = scanRecord[15]           //  mac地址

        temp[11] = scanRecord[16]           //  mac地址
        temp[12] = scanRecord[17]           //  mac地址
        temp[13] = scanRecord[18]           //  mac地址

        val result = HexUtil.formatHexString(temp) //  状态信息

//        L.e("解析到广播有效内容部分", "状态信息:${result.substring(0, 4)}")
//        L.e("info", "产品id:${result.substring(4, 8)}")
//        L.e("info", "mac地址:${result.substring(8, 20)}")
        L.e("解析到设备广播有效内容部分：", result)
        return result

    }

    /**
     * 读写的服务
     */
    internal fun findService(gatt: BluetoothGatt): BluetoothGattService? {
        return gatt.services?.find {
            it?.uuid.toString().startsWith(TO_PAIRED_START_KEY)
        }
    }

    /**
     * 读写通知的具体的特征
     */
    internal fun findCharacteristic(service: BluetoothGattService?): BluetoothGattCharacteristic? {
        return service?.characteristics?.find {
            it?.uuid.toString().startsWith(TO_PAIRED_START_KEY1)
        }
    }

    /**
     * 控制led灯闪烁
     * @param count 要设置闪烁的次数
     * @param intervalTime  要设置闪烁每次的时间  毫秒级 比如1000毫秒
     */
    internal fun setFlashInfo(count: Int = 3, intervalTime: Int = 1000): ByteArray {
        var time1 = 1000
        var time2 = 6000
        return if (intervalTime * count * 2 > 65535) {
            time2 = 65535
            time1 = 65535 / 2 / count
            var time1Str = Integer.toHexString(time1)
            time1Str = mathResult(time1Str)
            HexUtil.hexStringToBytes(
                "023004$time1Str${Integer.toHexString(time2)}"
            )
        } else {
            var time1Str = Integer.toHexString(intervalTime)
            time1Str = mathResult(time1Str)
            var time2Str = Integer.toHexString(intervalTime * count * 2)
            time2Str = mathResult(time2Str)
            HexUtil.hexStringToBytes("023004$time1Str$time2Str")
        }
    }

    private fun mathResult(timeStr: String): String {
        var timeStrTemp = timeStr
        when (timeStrTemp.length) {
            0 -> {
                timeStrTemp = "0000"
            }
            1 -> {
                timeStrTemp = "000$timeStrTemp"
            }
            2 -> {
                timeStrTemp = "00$timeStrTemp"
            }
            3 -> {
                timeStrTemp = "0$timeStrTemp"
            }
        }
        return timeStrTemp
    }

    /**
     * 扫描结果发现是长按的事件且未连接该设备
     */
    internal fun checkDeviceList(devices: List<BleDevice>): List<BleDevice> {
        return devices?.filter { device ->
            var find = false
            var result = parseRecord(device.scanRecord)
            result?.let {
                result = it.substring(0, 2)
                val content = it.subSequence(2, it.length)
                result?.let { type ->
                    when {
                        type.startsWith(VIP_CARD, true) -> {                            //  vip卡广播
                            content?.run {
                                when {
                                    startsWith(AFTER_PAIRED) -> {
                                        L.e(TAG, "配对成功后设备发送的广播-应要求已关闭自动链接功能，需要用户手动在app列表中点击")
                                        find = true
                                    }
                                    startsWith(LONG_PRESS) -> {
                                        L.e(TAG, "长按10秒配对的广播")
                                        find = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
            find
        }
    }

    /**
     * 扫描结果发现是长按的事件且未连接该设备
     */
    internal fun checkDevice(device: BleDevice?): BleDevice? {
        var result = device?.scanRecord?.let { parseRecord(it) }
        device?.let { LDBus.sendSpecial2(EVENT_TO_APP, ALL_FILTER_DEVICE, it) }

        result?.let {
            result = it.substring(0, 2)
            val content = it.subSequence(2, it.length)
            result?.let { type ->
                when {
                    type.startsWith(I_STATION, true) -> {                               //  基站广播
                        if (!FastSendIntercept.stationDoubleSend()) {
                            LDBus.sendSpecial2(EVENT_TO_APP, STATION_RESULT, content)               //  返回给app 状态信息2byte 产品id2byte mac地址6byte
                        }else{
                            L.e(TAG, "10秒内收到重复基站广播信号，只处理一次服务请求")
                        }
                    }
                    type.startsWith(VIP_CARD, true) -> {                                //  vip卡广播
                        content?.run {
                            when {
                                startsWith(AFTER_PAIRED) -> {
                                    L.e(TAG, "配对成功后设备发送的广播-应要求已关闭自动链接功能，需要用户手动在app列表中点击")
                                    return device
                                }
                                startsWith(LONG_PRESS) -> {
                                    L.e(TAG, "长按10秒配对的广播")
                                    return device
                                }
                                startsWith(DOUBLE_CLICK_CONN4)
                                        || startsWith(DOUBLE_CLICK_DIS_CONN5)
                                        || startsWith(DOUBLE_CLICK_DIS_CONN7) -> {
                                    if (!FastSendIntercept.doubleClickDoubleSend()) {
                                        when (EVENT_ID_CLICK) {                                     //  是点击事件
                                            substring(length - 6,length - 4) -> {
                                                when {
                                                    substring(length - 2,length) == EVENT_ID_CLICK_01 -> {   //  单击，事件不进行捕获
                                                        L.e(TAG, "点击事件-但不会进行事件的回传给app-广播形式的，只有建立通道才会进行回传")
                                                    }
                                                    substring(length - 2,length) == EVENT_ID_CLICK_02 -> {  //  双击进行发送
                                                        device?.let { bleDevice ->  LDBus.sendSpecial2(EVENT_TO_APP, DOUBLE_CLICK, bleDevice) }
                                                        L.e(TAG, "有效事件---->双击的广播")
                                                    }
                                                    else -> {
                                                        L.e(TAG, "点击的其他事件-非双击-也非单击")
                                                    }
                                                }
                                            }
                                            else -> {
                                                L.e(TAG, "非点击事件")
                                            }
                                        }
                                
                                    } else {
                                        L.e(TAG, "20秒内收到重复双击广播信号，只处理一次服务请求")
                                    }
                                }
                                else -> {
                                    L.e(TAG, "未配对过，需要用户长按进行配对后，才能扫描到")
                                }
                            }
                        }
                    }
                    else -> {
                        L.e(TAG, "啥也不是！")
                    }
                }
            }
        }
        return null
    }

    /**
     * 获取状态信息
     */
    fun getStateInfo(scanRecordStr: String?): String {
        scanRecordStr?.run {
            return substring(0, 4)
        }
        return ""
    }

    /**
     * 获取版本号信息
     */
    fun getVersionCode(scanRecordStr: String?): String {
        scanRecordStr?.run {
            var numB = hexToB(substring(2, 4))  //  得到二进制的数据
            val result1 = "${numB[1]}${numB[2]}${numB[3]}"
            val result2 = "${numB[4]}${numB[5]}${numB[6]}${numB[7]}"
            val result3 = bToInt(result1)
            val result4 = bToInt(result2)
            return "$result3.$result4"
        }
        return ""
    }

    /**
     * 获取是否可被连接或配对
     */
    fun isCanConnectionOrPair(scanRecordStr: String?): Boolean {
        scanRecordStr?.run {
            var numB = hexToB(substring(0, 2))  //  得到二进制的数据
            return numB[0].equals("1")
        }
        return false
    }

    /**
     * 是否是出厂状态 未配对
     * @return true为未配对 出厂状态
     *          false 为已配对
     */
    fun isFactoryState(scanRecordStr: String?): Boolean {
        scanRecordStr?.run {
            var numB = hexToB(substring(0, 2))  //  得到二进制的数据
            return numB[0].equals("1")
        }
        return false
    }

    /**
     * 是否包含event
     * @return true为包含
     *          false 为不包含
     */
    fun isContainEvent(scanRecordStr: String?): Boolean {
        scanRecordStr?.run {
            var numB = hexToB(substring(0, 2))  //  得到二进制的数据
            return numB[0].equals("1")
        }
        return false
    }

    /**
     * 十六进制转换成二进制
     */
    private fun hexToB(num: String): String {
        val i10 = Integer.parseInt(num, 16)
        var s2 = Integer.toBinaryString(i10)
        while (s2.length < 8) {
            s2 = "0$s2"
        }
        println("二进制：$s2 ")
        return s2
    }

    /**
     * 二进制转换成十六进制
     */
    private fun bToInt(numB: String): Int {
        return Integer.parseInt(numB, 2)
    }


    /**
     * 获取产品id
     */
    fun getProductId(scanRecordStr: String?): String {
        scanRecordStr?.run {
            return substring(4, 8)
        }
        return ""
    }

    /**
     * 获取mac地址
     */
    fun getMacId(scanRecordStr: String?): String {
        scanRecordStr?.run {
            return substring(8, 20)
        }
        return ""
    }

    /**
     * 对象单例
     */
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = ParseHelper()
    }
}