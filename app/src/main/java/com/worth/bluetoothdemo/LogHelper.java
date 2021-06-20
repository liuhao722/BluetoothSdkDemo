package com.worth.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.widget.TextView;

import com.clj.fastble.data.BleDevice;
import com.worth.framework.base.core.utils.L;

import java.util.List;
import java.util.Set;

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/19/21 --> 6:06 PM
 * Description: This is LogHelper
 */
public class LogHelper {
    private static final String TAG = "LogHelper";

    public static void logPrint(Set<BluetoothDevice> devices) {
        StringBuilder sb = new StringBuilder();
        while (devices.iterator().hasNext()) {
            BluetoothDevice item = devices.iterator().next();
            sb.append("蓝牙设备信息:\t" + item.getName())
                    .append("\tgetName:\t" + item.getName())
                    .append("\tgetAddress:\t" + item.getAddress())
                    .append("\tgetBondState:\t" + item.getBondState())
                    .append("\tscanRecord:\t" + item.getUuids().toString())
                    .append("\tscanRecord:\t" + item.getBluetoothClass().toString())
                    .append("\n");
        }
        L.e(TAG, sb.toString());
    }

    public static String printAndShowScanResult(List<BleDevice> list) {
        if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (BleDevice item : list) {
                sb.append("蓝牙名称:\t" + item.getName())
                        .append("\tmac地址:\t" + item.getMac())
                        .append("\trssi信号:\t" + item.getRssi())
                        .append("\tkey:\t" + item.getKey())
                        .append("\tdevices:\t" + item.getDevice().toString())
                        .append("\tscanRecord:\t" + item.getScanRecord().toString())
                        .append("\n");
            }
            L.e(TAG, sb.toString());
            return sb.toString();
        }
        return "";
    }
}
