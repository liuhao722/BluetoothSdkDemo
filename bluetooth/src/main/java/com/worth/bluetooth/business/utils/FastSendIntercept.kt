package com.worth.bluetooth.business.utils

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/21/21 --> 1:11 AM
 * Description: This is FastSendIntercept
 */
object FastSendIntercept {
    var lastTime: Long = 0
    private const val intervalTime = 20000L //  间隔

    /**
     * 快速发送双击会员卡事件的拦截
     */
    fun doubleSend(): Boolean {
        val curr = System.currentTimeMillis()
        if (curr - lastTime > intervalTime) {
            lastTime = curr
            return false
        }
        return true
    }
}