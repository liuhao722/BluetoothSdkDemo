package com.worth.bluetooth.business.ext

import com.worth.framework.base.core.storage.MeKV
import com.worth.framework.base.core.storage.MeKVUtil

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/14/21 --> 10:59 PM
 * Description: This is MeKVExt
 */
const val PAD_SDK_KEY_APP_MAC_ID =
    "pad_sdk_key_app_mac_id"                                         //  macId的key
const val PAD_SDK_KEY_PHONE_TYPE =
    "pad_sdk_key_phone_type"                                         //  手机类型

/**
 * 设置当前的macId
 */
fun MeKV.setMacId(macId: String) {
    MeKVUtil.set(PAD_SDK_KEY_APP_MAC_ID, macId)
}

/**
 * 获取当前的macId
 */
fun MeKV.getMacId() = MeKVUtil.get(PAD_SDK_KEY_APP_MAC_ID, "20:15:03:18:08:63")


/**
 * 设置当前的phoneType
 */
fun MeKV.setPhoneType(phoneTypeIsPad: Boolean) {
    MeKVUtil.set(PAD_SDK_KEY_PHONE_TYPE, phoneTypeIsPad)
}

/**
 * 获取当前的phoneType
 * @return phoneTypeIsPad true是pad false为手机
 */
fun MeKV.getPhoneType() = MeKVUtil.get(PAD_SDK_KEY_PHONE_TYPE, true)
