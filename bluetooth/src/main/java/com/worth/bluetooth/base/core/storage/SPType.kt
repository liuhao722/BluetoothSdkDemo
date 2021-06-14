package com.worth.bluetooth.base.core.storage

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * MMKV key定义
 * 对进程守护和特殊定义的key进行该出的定义
 */
enum class SPType(var key: String, var mode: Int) {
    EM_LOGIN("EM_LOGIN", MMKV.MULTI_PROCESS_MODE),  //双进程
    EM_NOTIFY("EM_NOTIFY", Context.MODE_PRIVATE),
}