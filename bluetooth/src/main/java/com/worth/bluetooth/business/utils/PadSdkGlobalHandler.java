package com.worth.bluetooth.business.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/30/21 --> 10:34 PM
 * Description: This is GlobalHandler
 */
public class PadSdkGlobalHandler {
    private static final String TAG = "GlobalHandler";
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

            }
        }
    };

    public WeakReference<Handler> mHandler = new WeakReference<>(handler);

    private PadSdkGlobalHandler() {
    }

    private static class SingletonHolder {
        public static PadSdkGlobalHandler instance = new PadSdkGlobalHandler();
    }

    public static PadSdkGlobalHandler ins() {
        return SingletonHolder.instance;
    }

}
