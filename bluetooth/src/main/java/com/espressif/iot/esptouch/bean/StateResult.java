package com.espressif.iot.esptouch.bean;

import java.net.InetAddress;

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/24/21 --> 10:54 PM
 * Description: This is StateResult
 */
public class StateResult {
    public CharSequence message = null;

    public boolean permissionGranted = false;

    public boolean locationRequirement = false;

    public boolean wifiConnected = false;
    public boolean is5G = false;
    public InetAddress address = null;
    public String ssid = null;
    public byte[] ssidBytes = null;
    public String bssid = null;
}
