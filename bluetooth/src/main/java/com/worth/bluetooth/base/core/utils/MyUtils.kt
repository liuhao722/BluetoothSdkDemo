package com.worth.bluetooth.base.core.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import kotlin.math.floor

// 两次点击按钮之间的点击间隔不能少于1000毫秒
private const val MIN_CLICK_DELAY_TIME = 1000
private var lastClickTime: Long = 0

// 两次点击按钮之间的点击间隔不能少于1000毫秒 判断
fun isFastClick(): Boolean {
    var flag = false
    val curClickTime = System.currentTimeMillis()
    if (curClickTime - lastClickTime >= MIN_CLICK_DELAY_TIME) {
        flag = true
    }
    lastClickTime = curClickTime
    return flag
}

/**
 * 获取用户是否开启了录音权限
 */
fun checkAudioRecordPermission(): Boolean {
    var lAudioRecorder: AudioRecord? = null
    try {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        lAudioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 8192
        )
        lAudioRecorder.startRecording()
        val len = lAudioRecorder.read(ByteArray(8 * 1024), 0, 8192)
        if (len <= 0) {
            if (lAudioRecorder != null) {
                lAudioRecorder.stop()
                lAudioRecorder.release()
                lAudioRecorder = null
            }
            return false
        }
    } catch (e: IllegalArgumentException) {
        lAudioRecorder?.release()
        lAudioRecorder = null
        e.printStackTrace()
        return false
    } catch (e: IllegalStateException) {
        if (lAudioRecorder!!.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            lAudioRecorder.stop()
        }
        lAudioRecorder?.release()
        lAudioRecorder = null
        e.printStackTrace()
        return false
    }
    if (lAudioRecorder != null) {
        lAudioRecorder.stop()
        lAudioRecorder.release()
        lAudioRecorder = null
    }
    return true
}

/**
 * 转换时间为时分秒
 */
fun secToTimeLong(time: Long): String? {
    var timeStr = ""
    var hour: Long = 0
    var minute: Long = 0
    var second: Long = 0
    var day: Long = 0
    if (time <= 0) {
        return "0分0秒"
    } else {
        minute = time / 60
        if (minute < 60) {
            second = time % 60
            timeStr = minute.toString() + "分" + second + "秒"
        } else {
            hour = minute / 60
            if (hour > 23) {
                day = hour / 24
                hour %= 24
                minute %= 60
                second = time - day * 24 * 3600 - hour * 3600 - minute * 60
                timeStr = day.toString() + "天" + hour + "时" + minute + "分" + second + "秒"
            } else {
                minute %= 60
                second = time - hour * 3600 - minute * 60
                timeStr = hour.toString() + "时" + minute + "分" + second + "秒"
            }
        }
    }
    return timeStr
}

/**
 * 分为单位的时间转化成  xxx小时xxx分
 *
 * @param time
 * @return
 */
fun convertTime(time: Long): String? {
    try {
        val hours = floor(time / 60.toDouble()).toInt()
        val minute = time % 60
        return hours.toString() + "小时" + minute + "分"
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return ""
}