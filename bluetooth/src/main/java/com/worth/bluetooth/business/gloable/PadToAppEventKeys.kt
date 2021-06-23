package com.worth.bluetooth.business.gloable


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/15/21 --> 11:41 PM
 */

// 发送集合
const val EVENT_TO_APP = "event_to_app_key"                                                         //  蓝牙sdk监听的事件回调
const val START_SCAN = 0x20_000_001                                                                 //  开始扫描
const val SCANNING = 0x20_000_002                                                                   //  扫描中
const val SCAN_FINISH = 0x20_000_003                                                                //  扫描结束

const val START_CONN = 0x20_000_010                                                                 //  扫描结束后-开始连接某个设备
const val CONN_FAIL = 0x20_000_011                                                                  //  扫描结束后-连接设备失败
const val CONN_OK = 0x20_000_012                                                                    //  扫描结束后-连接设备成功
const val DIS_CONN = 0x20_000_013                                                                   //  扫描结束后-在链接成功某个设备后，断开和某个设备的链接

const val WRITE_OK = 0x20_000_020                                                                   //  写入数据到设备成功
const val WRITE_FAIL = 0x20_000_021                                                                 //  写入数据到设备失败

const val PAIR_OK = 0x20_000_031                                                                    //  配对成功
const val PAIR_FAIL = 0x20_000_032                                                                  //  配对失败
const val PAIR_TIME_OUT = 0x20_000_033                                                              //  配对超时

const val CLICK = 0x20_000_301                                                                      //  单击
const val DOUBLE_CLICK = 0x20_000_302                                                               //  双击

const val STATION_RESULT = 0x20_000_400                                                             //  基站返回的数据



