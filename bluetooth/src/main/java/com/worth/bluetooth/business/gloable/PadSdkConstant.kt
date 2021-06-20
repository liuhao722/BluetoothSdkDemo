package com.worth.bluetooth.business.gloable

import com.clj.fastble.utils.HexUtil

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/15/21 --> 11:41 PM
 * Description: This is PadSdkConstant
 */

// 蓝牙指令发送集合

const val BLUETOOTH_TYPE_CLICK = 0xb101                                                             //  单击
const val BLUETOOTH_TYPE_DOUBLE_CLICK = 0xb102                                                      //  双击


const val SERVICE_UUID = "0XFFE0"
const val WRITE_UUID = "0XFFE1"
const val NOTIFY_UUID = "0XFFE1"


const val EVENT_TO_APP_KEY = "event_to_app_key"                                                     //  蓝牙sdk监听的事件回调
const val EVENT_START_SCAN = 0x20_000_001                                                           //  开始扫描
const val EVENT_SCANNING = 0x20_000_002                                                             //  扫描中
const val EVENT_SCAN_FINISH = 0x20_000_003                                                          //  扫描结束

const val EVENT_START_CONNECTION = 0x20_000_010                                                     //  扫描结束后-开始连接某个设备
const val EVENT_CONNECTION_FAIL = 0x20_000_011                                                      //  扫描结束后-连接设备失败
const val EVENT_CONNECTION_SUCCESS = 0x20_000_012                                                   //  扫描结束后-连接设备成功
const val EVENT_DIS_CONNECTION = 0x20_000_013                                                       //  扫描结束后-在链接成功某个设备后，断开和某个设备的链接


const val EVENT_TYPE = 0x20_000_300                                                                 //  按键事件
const val EVENT_TYPE_CLICK = 0x20_000_301                                                           //  单击
const val EVENT_TYPE_DOUBLE_CLICK = 0x20_000_302                                                    //  双击


const val UNPAIRED = "038011bb"                                                                     //  未配对
const val AFTER_PAIRED = "018011bb"                                                                 //  成功配对后的广播
const val LONG_PRESS = "0b8011bb"                                                                   //  长按10秒广播
@Deprecated(message = "单击无广播")
const val CLICK = "048011bb"                                                                        //  单击无广播
const val DOUBLE_CLICK_CONN4 = "048011bb"                                                           //  双击广播-4--连接状态下
const val DOUBLE_CLICK_DIS_CONN5 = "058011bb"                                                       //  双击广播-5--配对状态下断开链接
const val DOUBLE_CLICK_DIS_CONN7 = "078011bb"                                                       //  双击广播-7--未配对状态下断开链接

const val TO_PAIRED_START_KEY = "0000ffe0"                                                          //  配对时候的uuid前缀的service
const val TO_PAIRED_START_KEY1 = "0000ffe1"                                                         //  配对时候的clientUUid的前缀读写操作

// app-devices
val checkData: ByteArray = HexUtil.hexStringToBytes("02100104")                           //  配对请求的发送数据
val resultOk: ByteArray = HexUtil.hexStringToBytes("022001aa")                            //  配对验证成功
val resultFail: ByteArray = HexUtil.hexStringToBytes("02010101")                          //  配对验证失败
val toDeviceClickResult: ByteArray = HexUtil.hexStringToBytes("02010102")                 //  单击事件收到成功，返回应该是0102 不应该和错误的01混为一谈
// 闪烁规则-前面两位01f4闪烁的耗时 比如500毫秒。
// 比如需要闪烁三次500毫秒的话 就是500*3=1500 然后必须再乘以2 才是正确的闪烁频率，否则只闪烁一半，0bb8总的耗时（最终为3000）
val successToDeviceFlash: ByteArray = HexUtil.hexStringToBytes("02300401f40bb8")          //  配对成功，闪烁3次 500毫秒一次
val failToDeviceFlash: ByteArray = HexUtil.hexStringToBytes("023004006403e8")             //  配对失败，闪烁5次 100毫秒一次



const val RESULT_FIRST = "01"                                                                       //  返回的第一位
const val RESULT_DATA_TYPE_OK_FAIL_TIME_OUT = "01"                                                  //  dataType-返回错误、成功、超时
const val RESULT_DATA_TYPE_MATH_RESULT = "11"                                                       //  dataType-返回计算结果
const val RESULT_DATA_TYPE_MAC_ADDRESS = "20"                                                       //  dataType-返回mac地址
const val RESULT_DATA_TYPE_CLICK = "40"                                                             //  dataType-单机按键


const val RESULT_DATA_FAIL = "01"                                                                   //  data内容--返回错误
const val RESULT_DATA_OK = "02"                                                                     //  data内容--返回正确
const val RESULT_DATA_TIME_OUT = "03"                                                               //  data内容--返回超时
const val RESULT_DATA_CLICK = "01"                                                                  //  data内容--单击事件





