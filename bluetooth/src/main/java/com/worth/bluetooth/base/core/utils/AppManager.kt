package com.worth.bluetooth.base.core.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import com.worth.bluetooth.base.core.exts.nullTo
import java.lang.reflect.InvocationTargetException

var application: Application? = getApplicationBySelf()

@SuppressLint("PrivateApi")
private fun getApplicationBySelf(): Application? = application.nullTo {
    try {
        val activityThreadClass: Class<*> = Class.forName("android.app.ActivityThread")
        val appField = activityThreadClass.getDeclaredField("mInitialApplication")
        val method = activityThreadClass.getMethod("currentActivityThread", *arrayOfNulls(0))
        val currentAT = method.invoke(null)
        appField.isAccessible = true
        application = appField[currentAT] as Application
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    } catch (e: NoSuchFieldException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    }
    application
}

val debug: Boolean
    get() {
        return (0 != application?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_DEBUGGABLE))
    }

/**
 * 获取当前环境是否是debug状态
 */
val debugConfig : Boolean = getAppMetaDataByBoolean("is_Debug")

// 环境变量
val channelNumber: Int = if (getAppMetaDataByString("ENVIRONMENT_VALUE") == "release"){
    1
}else{
    2
}

val loggerEnable: Int = getAppMetaDataByInt("LOG_ENABLE")

// 渠道id
val channel: String = getAppMetaDataByString("CHANNEL")

// 当前应用版本号
val versionCode = getAppVersionCode()

// 当前应用版本号名称
val versionName = getAppVersionName()

// 当前设备的mac地址
val macId = getPhoneMacId()

// 域名类型
val networkEnv: String = getAppMetaDataByString("NETWORK_ENV")

/**
 * 获取String类型的meta标签
 */
fun getAppMetaDataByString(key: String): String = application?.packageName?.let {
    try {
        val meta = application?.packageManager?.getApplicationInfo(it, PackageManager.GET_META_DATA)
        meta?.metaData?.getString(key) ?: ""
    } catch (e: Exception) {
        return ""
    }
} ?: ""

/**
 * 获取Int类型的meta标签
 */
fun getAppMetaDataByInt(key: String): Int = application?.packageName?.let {
    val meta = application?.packageManager?.getApplicationInfo(it, PackageManager.GET_META_DATA)
    meta?.metaData?.getInt(key) ?: 0
} ?: 0

/**
 * 获取Boolean类型的meta标签 默认为false
 */
fun getAppMetaDataByBoolean(key: String): Boolean = application?.packageName?.let {
    val meta = application?.packageManager?.getApplicationInfo(it, PackageManager.GET_META_DATA)
    meta?.metaData?.getBoolean(key, false) ?: false
} ?: false

/**
 * 获取当前应用版本号
 */
fun getAppVersionCode(): Int = application?.applicationContext?.run {
    packageManager.getPackageInfo(packageName, 0).versionCode
} ?: -1

/**
 * 获取当前应用版本号名称
 */
fun getAppVersionName(): String = application?.applicationContext?.let {
    it.packageManager.getPackageInfo(it.packageName, 0).versionName
} ?: "-1"

/**
 * 获取当前设备的mac地址
 */
@SuppressLint("HardwareIds")
fun getPhoneMacId() = application?.applicationContext?.getSystemService(Context.WIFI_SERVICE).let {
    (it as WifiManager).connectionInfo.macAddress
} ?: ""

/**
 * 根据包名获取版本号
 */
fun getLocalAppVersion(packageName: String): Int { //获取当前设备已安装 APP版本号
    return try {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        val packageInfo: PackageInfo? = application?.packageManager?.getPackageInfo(packageName, 0)
        packageInfo?.versionCode ?: 0
    } catch (e: java.lang.Exception) {
        0
    }
}

