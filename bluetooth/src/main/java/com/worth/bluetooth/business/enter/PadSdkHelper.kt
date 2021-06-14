package com.worth.bluetooth.business.enter

import com.worth.bluetooth.base.core.storage.MeKVUtil
import com.worth.bluetooth.base.core.utils.application

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/25/21 --> 4:45 PM
 * Description: This is VipSdkHelper
 */
class PadSdkHelper private constructor() {
    /**
     * 初始化操作
     */
    init {
        application?.let {
            MeKVUtil.initMMKV(it)
        }
    }

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
    fun setPhoneType() {

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
