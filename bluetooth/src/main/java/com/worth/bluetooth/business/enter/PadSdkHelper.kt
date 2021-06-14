package com.worth.bluetooth.business.enter

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/25/21 --> 4:45 PM
 * Description: This is VipSdkHelper
 */
class PadSdkHelper private constructor() {

    /**
     * 初始化sdk
     * @param host  域名地址
     * @param httpHeaders httpHeader要在app中设置的内容
     * @param httpBody  body要设置的内容
     */
    @JvmOverloads
    fun initPadSdk(
        host: String?,
        httpHeaders: MutableMap<String, Any>?,
        httpBody: MutableMap<String, Any>?
    ): PadSdkHelper {
        return this
    }

    /**
     * 设置使用设备的类型
     */
    fun setPhoneType(isPad: Boolean = true) {

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
