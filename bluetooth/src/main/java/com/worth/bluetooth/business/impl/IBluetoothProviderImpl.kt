package com.worth.bluetooth.business.impl

import android.content.Context
import com.worth.framework.base.core.bean.BluetoothBean
import com.worth.framework.base.core.router.provider.IBluetoothProvider

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/14/21 --> 11:46 PM
 * Description: This is IBluetoothProviderImpl
 */
class IBluetoothProviderImpl : IBluetoothProvider {
    override fun appSendMsgToVipCard(cmd: String?) {
    }

    override fun clickVipCard() {
    }

    override fun connect() {
    }

    override fun disconnect() {
    }

    override fun doubleClickVipCard() {
    }

    override fun init(context: Context?) {
    }

    override fun listenerBluetoothSearch(): List<BluetoothBean> {
        return mutableListOf()
    }

    override fun pressVipCard() {
    }

    override fun setMacId(macId: String) {
    }

    override fun wifiConn() {
    }
}