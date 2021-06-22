package com.worth.bluetoothdemo;

import com.clj.fastble.data.BleDevice;
import com.worth.framework.base.core.utils.L;

import java.util.List;

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/19/21 --> 6:06 PM
 * Description: This is LogHelper
 */
public class LogHelper {
    private static final String TAG = "LogHelper";

    public static String printAndShowScanResult(List<BleDevice> list) {
        if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (BleDevice item : list) {
                sb.append((list.indexOf(item) + 1) + "、蓝牙名称: " + item.getName())
                        .append("\t\tmac地址: " + item.getMac())
                        .append("\t\t信号量:" + item.getRssi())
                        .append("\n")
//                        .append("\tBluetoothDevice: " + item.getDevice().toString())
                ;
            }
            L.e(TAG, sb.toString());
            return sb.toString();
        }
        return "";
    }
}
