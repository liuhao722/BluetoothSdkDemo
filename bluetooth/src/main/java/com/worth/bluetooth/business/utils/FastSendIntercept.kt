package com.worth.bluetooth.business.utils

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/21/21 --> 1:11 AM
 * Description: This is FastSendIntercept
 */
object FastSendIntercept {
    private var lastDoubleClickTime: Long = 0
    private const val doubleClickIntervalTime = 10_000L //  间隔

    /**
     * 快速发送双击会员卡事件的拦截
     */
    internal fun doubleClickDoubleSend(): Boolean {
        val curr = System.currentTimeMillis()
        if (curr - lastDoubleClickTime > doubleClickIntervalTime) {
            lastDoubleClickTime = curr
            return false
        }
        return true
    }

    /**********************************************************************************************/

    private var lastStationSendTime: Long = 0
    private const val lastStationIntervalTime = 10_000L //  间隔

    /**
     * 快速发送双击会员卡事件的拦截
     */
    internal fun stationDoubleSend(): Boolean {
        val curr = System.currentTimeMillis()
        if (curr - lastStationSendTime > lastStationIntervalTime) {
            lastStationSendTime = curr
            return false
        }
        return true
    }


}