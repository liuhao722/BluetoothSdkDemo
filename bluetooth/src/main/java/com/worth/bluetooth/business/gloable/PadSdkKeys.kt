package com.worth.bluetooth.business.gloable

import com.clj.fastble.utils.HexUtil.*

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/21/21 --> 7:34 PM
 * Description: This is PadSdkKeys
 */

const val I_STATION = "af"                                                                          //  基站的广播
const val VIP_CARD = "0f"                                                                           //  VIP卡的广播

const val EVENT_ID_CLICK = "b1"                                                                     //  click的事件
const val EVENT_ID_CLICK_01 = "01"                                                                  //  click的事件--单击不进行处理
const val EVENT_ID_CLICK_02 = "02"                                                                  //  click的事件--双击回传

const val UNPAIRED = "038011bb"                                                                     //  未配对
const val AFTER_PAIRED = "018011bb"                                                                 //  成功配对后的广播
const val LONG_PRESS = "0b8011bb"                                                                   //  长按10秒广播
const val DOUBLE_CLICK_CONN4 = "048011bb"                                                           //  双击广播-4--连接状态下
const val DOUBLE_CLICK_DIS_CONN5 = "058011bb"                                                       //  双击广播-5--配对状态下断开链接
const val DOUBLE_CLICK_DIS_CONN7 = "078011bb"                                                       //  双击广播-7--未配对状态下断开链接

const val TO_PAIRED_START_KEY = "0000ffe0"                                                          //  配对时候的uuid前缀的service
const val TO_PAIRED_START_KEY1 = "0000ffe1"                                                         //  配对时候的clientUUid的前缀读写操作

// app->devices 数据
val checkData: ByteArray = hexStringToBytes("02100104")                                   //  配对请求的发送数据
val resultOk: ByteArray = hexStringToBytes("022001aa")                                    //  配对验证成功
val resultFail: ByteArray = hexStringToBytes("02010101")                                  //  配对验证失败
val toDeviceClickResult: ByteArray = hexStringToBytes("02010102")                         //  单击事件收到成功，返回应该是0102 不应该和错误的01混为一谈

// 闪烁规则-前面两位01f4闪烁的耗时 比如500毫秒。
// 比如需要闪烁三次500毫秒的话 就是500*3=1500 然后必须再乘以2 才是正确的闪烁频率，否则只闪烁一半，0bb8总的耗时（最终为3000）
val successToDeviceFlash: ByteArray = hexStringToBytes("02300401f40bb8")                  //  配对成功，闪烁3次 500毫秒一次
val failToDeviceFlash: ByteArray = hexStringToBytes("023004006403e8")                     //  配对失败，闪烁5次 100毫秒一次

//  校验返回结果的开头数据部分
const val RESULT_FIRST = "01"                                                                       //  返回的第一位
const val RESULT_DATA_TYPE_1 = "01"                                                                 //  dataType-返回错误、成功、超时
const val RESULT_DATA_TYPE_2 = "11"                                                                 //  dataType-返回计算结果
const val RESULT_DATA_TYPE_3 = "20"                                                                 //  dataType-返回mac地址
const val RESULT_DATA_TYPE_4 = "40"                                                                 //  dataType-单机按键

//  校验返回结果的数据部分
const val RESULT_DATA_FAIL = "01"                                                                   //  data内容--返回错误
const val RESULT_DATA_OK = "02"                                                                     //  data内容--返回正确
const val RESULT_DATA_TIME_OUT = "03"                                                               //  data内容--返回超时

