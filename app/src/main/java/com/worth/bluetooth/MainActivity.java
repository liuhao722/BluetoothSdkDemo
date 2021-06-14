package com.worth.bluetooth;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.worth.bluetooth.business.enter.PadSdkHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final int PERMISSION_REQUEST_CODE = 1000;
    private PadSdkHelper padSdkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();       //  初始化百度sdk依赖的权限
        initView();
        initSdk();              //  初始化sdk
        initObserver();         //  监听sdk错误的返回
    }

    private void initSdk() {
        padSdkHelper = PadSdkHelper.Companion.getInstance().initPadSdk();
    }


    private void initObserver() {

    }

    private void initView() {

    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, // demo使用
                Manifest.permission.BLUETOOTH,
                /* 下面是蓝牙用的，可以不申请
                Manifest.permission.BROADCAST_STICKY,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
                */
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), PERMISSION_REQUEST_CODE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        if (requestCode == PERMISSION_REQUEST_CODE) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}