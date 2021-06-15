package com.espressif.iot.esptouch.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

public class NetHelper {
    public static boolean isWifiConnected(WifiManager wifiManager, Context context) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo != null
                && wifiInfo.getNetworkId() != -1
                && !"<unknown ssid>".equals(getWifiSSID(context));
    }
    /**********************************************************************************************/

    private static final String WIFI_SSID_UNKNOWN = WifiManager.UNKNOWN_SSID;

    public static String getWifiSSID(Context context) {
        /*
         *  先通过 WifiInfo.getSSID() 来获取
         */
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : null;
        String result = wifiId != null ? wifiId.trim() : null;
        if (!TextUtils.isEmpty(result)) {
            // 部分机型上获取的 ssid 可能会带有 引号
            if (result.charAt(0) == '"' && result.charAt(result.length() - 1) == '"') {
                result = result.substring(1, result.length() - 1);
            }
        }
        // 如果上面通过 WifiInfo.getSSID() 来获取到的是 空或者 <unknown ssid>，则使用 networkInfo.getExtraInfo 获取
        if (TextUtils.isEmpty(result) || WIFI_SSID_UNKNOWN.equalsIgnoreCase(result.trim())) {
            NetworkInfo networkInfo = getNetworkInfo(context);
            if (networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo() != null){
                    result = networkInfo.getExtraInfo().replace("\"","");
                }
            }
        }
        // 如果获取到的还是 空或者 <unknown ssid>，则遍历 wifi 列表来获取
        if (TextUtils.isEmpty(result) || WIFI_SSID_UNKNOWN.equalsIgnoreCase(result.trim())) {
            result = getSSIDByNetworkId(context);
        }
        return result;
    }

    public static NetworkInfo getNetworkInfo(Context context){
        try{
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null != connectivityManager){
                return connectivityManager.getActiveNetworkInfo();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /*
     *  遍历wifi列表来获取
     */
    private static String getSSIDByNetworkId(Context context) {
        String ssid = WIFI_SSID_UNKNOWN;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int networkId = wifiInfo.getNetworkId();
            @SuppressLint("MissingPermission")
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : configuredNetworks){
                if (wifiConfiguration.networkId == networkId){
                    ssid = wifiConfiguration.SSID;
                    break;
                }
            }
        }
        return ssid;
    }

    /**********************************************************************************************/
    public static byte[] getRawSsidBytes(WifiInfo info) {
        try {
            Method method = info.getClass().getMethod("getWifiSsid");
            method.setAccessible(true);
            Object wifiSsid = method.invoke(info);
            if (wifiSsid == null) {
                return null;
            }
            method = wifiSsid.getClass().getMethod("getOctets");
            method.setAccessible(true);
            return (byte[]) method.invoke(wifiSsid);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getRawSsidBytesOrElse(WifiInfo info, byte[] orElse) {
        byte[] raw = getRawSsidBytes(info);
        return raw != null ? raw : orElse;
    }

    public static String getSsidString(Context context) {
        return getWifiSSID(context);
    }

    public static InetAddress getBroadcastAddress(WifiManager wifi) {
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp != null) {
            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++) {
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            }
            try {
                return InetAddress.getByAddress(quads);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        try {
            return InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // Impossible arrive here
        return null;
    }

    public static boolean is5G(int frequency) {
        return frequency > 4900 && frequency < 5900;
    }

    public static InetAddress getAddress(int ipAddress) {
        byte[] ip = new byte[]{
                (byte) (ipAddress & 0xff),
                (byte) ((ipAddress >> 8) & 0xff),
                (byte) ((ipAddress >> 16) & 0xff),
                (byte) ((ipAddress >> 24) & 0xff)
        };

        try {
            return InetAddress.getByAddress(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            // Impossible arrive here
            return null;
        }
    }

    private static InetAddress getAddress(boolean isIPv4) {
        try {
            Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
            while (enums.hasMoreElements()) {
                NetworkInterface ni = enums.nextElement();
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress address = addrs.nextElement();
                    if (!address.isLoopbackAddress()) {
                        if (isIPv4 && address instanceof Inet4Address) {
                            return address;
                        }
                        if (!isIPv4 && address instanceof Inet6Address) {
                            return address;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InetAddress getIPv4Address() {
        return getAddress(true);
    }

    public static InetAddress getIPv6Address() {
        return getAddress(false);
    }

    /**
     * @param bssid the bssid like aa:bb:cc:dd:ee:ff
     * @return byte array converted from bssid
     */
    public static byte[] convertBssid2Bytes(String bssid) {
        String[] bssidSplits = bssid.split(":");
        if (bssidSplits.length != 6) {
            throw new IllegalArgumentException("Invalid bssid format");
        }
        byte[] result = new byte[bssidSplits.length];
        for (int i = 0; i < bssidSplits.length; i++) {
            result[i] = (byte) Integer.parseInt(bssidSplits[i], 16);
        }
        return result;
    }
}
