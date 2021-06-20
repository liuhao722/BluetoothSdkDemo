package com.worth.bluetooth.business.gloable

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/15/21 --> 11:41 PM
 * Description: This is PadSdkConstant
 */

// 蓝牙指令发送集合

const val BLUETOOTH_TYPE = 0xb1                                                                     //  按键事件
const val BLUETOOTH_TYPE_CLICK = 0x01                                                               //  单击
const val BLUETOOTH_TYPE_DOUBLE_CLICK = 0x02                                                        //  双击


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



