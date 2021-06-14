package com.worth.bluetooth.base.core.router.provider

import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.template.IProvider
import com.worth.bluetooth.base.core.router.BluetoothConnectionProvider

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/14/21 --> 5:21 AM
 * Description: This is IBluetoothProvider
 * 1、单击-会员卡：唤醒语音助手
 * 2、双击-会员卡：结账
 * 3、长按-会员卡：绑卡信号，成功绑定则推送至卡广播，闪烁蓝牙一下
 */
@Route(
    path = BluetoothConnectionProvider.PATH_BLUETOOTH_PROVIDER,
    name = "提供链接，断开链接，返回设备扫描信息，单击事件-唤醒语音助手，双击事件-结账，长按事件 "
)
interface IBluetoothProvider : IProvider {
    /**
     * 链接
     */
    fun connect()

    /**
     * 断开
     */
    fun disconnect()

    /**
     *
     */
    fun appSendMsgToVipCard()

    /**
     * 单击会员卡--唤醒语音助手
     */
    fun clickVipCard()

    /**
     * 双击会员卡--结账
     */
    fun doubleClickVipCard()

    /**
     * 长按会员卡--绑卡信号，成功绑定则推送至卡广播，闪烁蓝牙一下
     */
    fun pressVipCard()
}