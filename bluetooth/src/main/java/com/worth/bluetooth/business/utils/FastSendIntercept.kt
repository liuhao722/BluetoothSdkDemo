package com.worth.bluetooth.business.utils

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/21/21 --> 1:11 AM
 * Description: This is FastSendIntercept
 */
object FastSendIntercept {
    var lastTime: Long = 0
    private const val intervalTime = 8000L //  间隔

    /**
     * 双击拦截
     */
    fun doubleSendIntercept(): Boolean {
        val curr = System.currentTimeMillis()
        if (curr - lastTime > intervalTime) {
            lastTime = curr
            return true
        }
        return false
    }
}