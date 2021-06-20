package com.worth.bluetooth.business.utils

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import com.clj.fastble.utils.HexUtil
import com.worth.bluetooth.business.gloable.TO_PAIRED_START_KEY
import com.worth.bluetooth.business.gloable.TO_PAIRED_START_KEY1


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

//        Log.e("info", "状态信息:${result.substring(0, 4)}")
//        Log.e("info", "产品id:${result.substring(4, 8)}")
//        Log.e("info", "mac地址:${result.substring(8, 20)}")
        Log.e("info", result)
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
     * 对象单例
     */
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = ParseHelper()
    }
}
