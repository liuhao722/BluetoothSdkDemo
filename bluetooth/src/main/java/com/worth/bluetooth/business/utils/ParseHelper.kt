package com.worth.bluetooth.business.utils

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import com.clj.fastble.data.BleDevice
import com.clj.fastble.utils.HexUtil
import com.worth.bluetooth.business.gloable.*
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
        val temp = ByteArray(10)
        temp[0] = scanRecord[6]             //  状态信息
        temp[1] = scanRecord[7]             //  状态信息

        temp[2] = scanRecord[8]             //  产品id
        temp[3] = scanRecord[9]             //  产品id

        temp[4] = scanRecord[10]            //  mac地址
        temp[5] = scanRecord[11]            //  mac地址
        temp[6] = scanRecord[12]            //  mac地址
        temp[7] = scanRecord[13]            //  mac地址
        temp[8] = scanRecord[14]            //  mac地址
        temp[9] = scanRecord[15]            //  mac地址

        val result = HexUtil.formatHexString(temp) //  状态信息

        Log.e("解析到广播有效内容部分", "状态信息:${result.substring(0, 4)}")
//        Log.e("info", "产品id:${result.substring(4, 8)}")
//        Log.e("info", "mac地址:${result.substring(8, 20)}")
//        Log.e("解析到广播有效内容部分：", result)
        return result

    }

    /**
     * 读写的服务
     */
    fun findService(gatt: BluetoothGatt): BluetoothGattService? {
        return gatt.services?.find {
            it?.uuid.toString().startsWith(TO_PAIRED_START_KEY)
        }
    }

    /**
     * 读写通知的具体的特征
     */
    fun findCharacteristic(service: BluetoothGattService?): BluetoothGattCharacteristic? {
        return service?.characteristics?.find {
            it?.uuid.toString().startsWith(TO_PAIRED_START_KEY1)
        }
    }

    /**
     * 控制led灯闪烁
     * @param count 要设置闪烁的次数
     * @param intervalTime  要设置闪烁每次的时间  毫秒级 比如1000毫秒
     */
    fun setFlashInfo(count: Int = 3, intervalTime: Int = 1000): ByteArray{
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
            var time2Str = Integer.toHexString(intervalTime*count*2)
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
    fun checkDeviceList(devices: List<BleDevice>): List<BleDevice> {
        return devices?.filter { device ->
            var find = false
            val result = ParseHelper.instance.parseRecord(device.scanRecord)
            result?.run {
                when {
                    startsWith(AFTER_PAIRED) -> {
//                            connectionAndNotify(bleDevice, true)                                  //  应要求关闭
                        Log.e(TAG, "配对成功后设备发送的广播-应要求已关闭自动链接功能，需要用户手动在app列表中点击")
                        find = true
                    }
                    startsWith(LONG_PRESS) -> {
                        Log.e(TAG, "长按10秒配对的广播")
//                            connectionAndNotify(bleDevice, false)                                 //  执行配对流程--关闭，扫描时候如果是未配对的状态下，不进行数据的返回
                        find = true
                    }
                    startsWith(DOUBLE_CLICK_CONN4)
                            || startsWith(DOUBLE_CLICK_DIS_CONN5)
                            || startsWith(DOUBLE_CLICK_DIS_CONN7) -> {
                        if (!FastSendIntercept.doubleSend()) {
                            LDBus.sendSpecial2(EVENT_TO_APP, DOUBLE_CLICK, device)
                            Log.e(TAG, "有效事件---->已连接时候——双击的广播")
                        } else {
                            Log.e(TAG, "20秒内收到重复双击广播信号，只处理一次服务请求")
                        }
                        find = false
                    }
                    else -> {
                        Log.e(TAG, "未配对过，需要用户长按进行配对后，才能扫描到")
                        find = false
                    }
                }
            }
            find
        }
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
