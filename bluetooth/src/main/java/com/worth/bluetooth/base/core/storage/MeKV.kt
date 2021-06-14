package com.worth.bluetooth.base.core.storage

/**
 * 封装简化获取存储的数据
 */
const val APP_HOST = "app_set_app_host"
const val AI_LIST = "app_set_ai_list"
const val WAKE_UP_SWITCH = "app_set_wake_up_switch"

/**
 * 存储类
 */
object MeKV {

    /**********************************************************************************************/
    /**
     * 获取当前的host
     */
    fun getAiInstructionSetKey() = MeKVUtil.get(AI_LIST, "query")

    /**
     * 设置当前的host
     */
    fun setAiInstructionSet(aiSet: String) = MeKVUtil.set(AI_LIST, aiSet)

    /**********************************************************************************************/
    /**
     * 获取当前的host
     */
    fun getHost() = MeKVUtil.get(APP_HOST, "http://192.168.0.103:8080/api/v1/service/chat")

    /**
     * 设置当前的host
     */
    fun setHost(host: String) = MeKVUtil.set(APP_HOST, host)


    /**********************************************************************************************/
    /**
     * 用户设置唤醒的开关状态
     */
    fun setWakeUpSwitch(switch: Boolean) = MeKVUtil.set(WAKE_UP_SWITCH, switch)

    /**
     * 获取用户设置的唤醒开关状态
     */
    fun wakeUpSwitchIsOpen() = MeKVUtil.get(WAKE_UP_SWITCH, false)


    /**********************************************************************************************/

}