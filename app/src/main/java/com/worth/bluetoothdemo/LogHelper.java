package com.worth.bluetoothdemo;

import android.view.Gravity;
import android.widget.Toast;

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
        if (list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (BleDevice item : list) {
            sb.append(
                    (list.indexOf(item) + 1) + "、蓝牙名称: " + item.getName())
                    .append("\t\tmac地址: " + item.getMac())
                    .append("\t\t信号量:" + item.getRssi())
                    .append("\n")
//                    .append("\tBluetoothDevice: " + item.getDevice().toString())
            ;
        }
        L.e(TAG, sb.toString());
        return sb.toString();
    }

    public static void toast(String msg, int offsetY) {
        // 此时可以loading展示--自行替换就可以了
        Toast toast = Toast.makeText(App.context, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, offsetY);
        toast.show();
    }
}
