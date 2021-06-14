package com.worth.bluetooth.base.core.gson

import android.text.TextUtils
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.worth.bluetooth.base.core.exts.notnullTo
import java.lang.reflect.Type
import java.util.*

/**
 * Description : json 转换工具类
 * Created by YW on 2019-12-26.
 * Email：1809267944@qq.com
 */
object JsonUtil {
    private var mGson: Gson? = Gson()
    open var mMapGson: Gson? = Gson()

    private fun getGson(): Gson? {
        if (mGson == null) mGson = Gson()
        return mGson
    }

    /**
     * 对象转换成String
     *
     * @param src 待转换的对象
     * @return json string
     */
    fun toJson(src: Any?): String {
        return if (src == null) {
            ""
        } else getGson()!!.toJson(src)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> fromJson(json: String, cls: Class<T>): T? {
        return try {
            if (TextUtils.isEmpty(json)) {
                null
            } else {
//                if (cls.javaType == String::class.java.javaType) {
//                    json as T
//                } else {
                    getGson()!!.fromJson(json, cls)
//                }
            }
        } catch (e: JsonSyntaxException) {
            e.stackTrace
            null
        }
    }

    /**
     * json string 转换成object
     * @param json 待转换的json string
     * @param typeOfT 需要转换的类型
     * @param <T> 需要转换的反省
     * @return 返回的转换结果
     */
    fun <T> fromJsonT(json: String, typeOfT: Type, t: T): T? {
        return try {
            if (TextUtils.isEmpty(json)) {
                null
            } else getGson()!!.fromJson<T>(json, typeOfT)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    inline fun <reified T> fromJson(json: String): T? {
        return mMapGson?.fromJson(json, object: TypeToken<T>(){}.type)
    }

    fun <T> fromJsonMap(json: String, typeOfT: Type): T? {
        if (mMapGson == null) {
            mMapGson =
                GsonBuilder().registerTypeAdapter(object : TypeToken<Map<String, Any>>() {}.type, MapTypeAdapter())
                        .create()
        }
        return mMapGson!!.fromJson(json, typeOfT)
    }

    @Suppress("DEPRECATION")
    fun <T> fromJsonList(json: String, cls: Class<T>): MutableList<T>? {
        try {
            return if (TextUtils.isEmpty(json)) {
                null
            } else {
                val list = ArrayList<T>()
                val array = JsonParser().parse(json).asJsonArray
                if (null != array && array.size() > 0) {
                    val iterator = array.iterator()
                    while (iterator.hasNext()) {
                        val elem = iterator.next() as JsonElement
                        list.add(getGson()!!.fromJson(elem, cls)!!)
                    }
                }
                list
            }
        } catch (e: JsonSyntaxException) {
            return null
        }
    }

    /**
     * 判断是否为json格式数据
     */
    fun isJson(str: String): Boolean {
        return !TextUtils.isEmpty(str) && ((str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]")))
    }

    /**
     * 格式化json数据
     */
    fun formatJson(jsonStr: String?): String {
        val sb = StringBuilder()
        jsonStr?.notnullTo {
            var last: Char
            var current = '\u0000'
            var index = 0
            var isInQuotationMarks = false
            for (element in jsonStr) {
                last = current
                current = element
                when (current) {
                    '"' -> {
                        if (last != '\\') {
                            isInQuotationMarks = !isInQuotationMarks
                        }
                        sb.append(current)
                    }
                    '{', '[' -> {
                        sb.append(current)
                        if (!isInQuotationMarks) {
                            sb.append('\n')
                            index++
                            addIndentBlank(sb, index)
                        }
                    }
                    '}', ']' -> {
                        if (!isInQuotationMarks) {
                            sb.append('\n')
                            index--
                            addIndentBlank(sb, index)
                        }
                        sb.append(current)
                    }
                    ',' -> {
                        sb.append(current)
                        if (last != '\\' && !isInQuotationMarks) {
                            sb.append('\n')
                            addIndentBlank(sb, index)
                        }
                    }
                    else -> sb.append(current)
                }
            }
        }
        return sb.toString()
    }

    /**
     * 添加空行
     */
    private fun addIndentBlank(sb: StringBuilder, indent: Int) {
        for (i in 0 until indent) {
            sb.append('\t')
        }
    }
}