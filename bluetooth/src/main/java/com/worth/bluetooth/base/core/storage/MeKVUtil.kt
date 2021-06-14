package com.worth.bluetooth.base.core.storage

import android.content.Context
import com.tencent.mmkv.MMKV
import com.worth.bluetooth.base.core.exts.nullTo
import com.worth.bluetooth.base.core.exts.todo
import com.worth.bluetooth.base.core.utils.L

/**
 * mmkv 封装类
 */
object MeKVUtil {
    // TPS 初始化mmkv状态值
    private var isInit = false

    /**
     * 初始化mmkv
     */
    fun initMMKV(context: Context) {
        val rootDir: String = MMKV.initialize(context)
        println("mmkv root: $rootDir")
        isInit = true
    }

    /**
     * 根据[key]设置存储值[value]
     */
    fun set(key: String, value: Any) {
        setValue(key = key, value = value)
    }

    /**
     * 指定空间[spType]根据[key]设置存储值[value]
     */
    fun set(spType: SPType, key: String, value: Any) {
        setValue(spType.key, key, value, spType.mode)
    }

    /**
     * 根据[key]获取默认空间的存储值[defValue]
     */
    fun <T> get(key: String?, defValue: T): T {
        return getValue(key = key, defValue = defValue)
    }

    /**
     * 指定空间[spType]根据[key]获取的存储值[defValue]
     */
    fun <T> get(spType: SPType, key: String?, defValue: T): T {
        return getValue(spType.key, key, defValue, spType.mode)
    }

    /**
     * 删除[spType]空间中的某个[key]对应的存储
     */
    fun remove(spType: SPType, key: String) {
        getMMKV(spType.key, spType.mode)?.removeValueForKey(key)
    }

    /**
     * 删除默认空间中的某个[key]对应的存储
     */
    fun remove(key: String) {
        getMMKV()?.removeValueForKey(key)
    }

    /**
     * 清除指定空间[spType]的存储,[spType]可以为空
     */
    fun clearKey(spType: SPType? = null) {
        spType.todo({
            getMMKV()
        }, { getMMKV(spType?.key, spType?.mode ?: Context.MODE_PRIVATE) })?.clearAll()
    }


    @Suppress("UNCHECKED_CAST")
    private fun <T> getValue(
        mmapId: String? = null,
        key: String?,
        defValue: T,
        mode: Int = Context.MODE_PRIVATE
    ): T {
        if (!isInit) {
            throw IllegalArgumentException("please init mmkv -> initMMKV()")
        }
        val kv = getMMKV(mmapId, mode)
        var result: Any? = null
        when (defValue) {
            is Int -> {
                result = kv?.decodeInt(key, (defValue as Int))
            }
            is Long -> {
                result = kv?.decodeLong(key, (defValue as Long))
            }
            is Float -> {
                result = kv?.decodeFloat(key, (defValue as Float))
            }
            is Double -> {
                result = kv?.decodeDouble(key, (defValue as Double))
            }
            is Boolean -> {
                result = kv?.decodeBool(key, (defValue as Boolean))
            }
            is String -> {
                result = kv?.decodeString(key, defValue as String)
            }
            is ByteArray -> {
                result = kv?.decodeBytes(key)
            }
        }
        result.nullTo {
            result = defValue
            result
        }
        return result as T
    }

    private fun setValue(
        mmapId: String? = null,
        key: String,
        value: Any,
        mode: Int = Context.MODE_PRIVATE
    ) {
        if (!isInit) {
            throw IllegalArgumentException("please init mmkv -> initMMKV()")
        } else {
            val kv = getMMKV(mmapId, mode)
            val result = when (value) {
                is Int -> {
                    kv?.encode(key, value)
                }
                is Long -> {
                    kv?.encode(key, value)
                }
                is Float -> {
                    kv?.encode(key, value)
                }
                is Double -> {
                    kv?.encode(key, value)
                }
                is Boolean -> {
                    kv?.encode(key, value)
                }
                is String -> {
                    kv?.encode(key, value)
                }
                is ByteArray -> {
                    kv?.encode(key, value)
                }
                else -> false
            }
            L.d("set-$result")
        }

    }

    /**
     * 根据唯一表示id[mmapId]和类型[mode]获取mmkv对象
     */
    private fun getMMKV(mmapId: String? = null, mode: Int = Context.MODE_PRIVATE) =
        mmapId.todo({ MMKV.defaultMMKV() }, { MMKV.mmkvWithID(mmapId, mode) })
}