package com.worth.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.worth.bluetooth.business.enter.PadSdkHelper;
import com.worth.framework.base.core.utils.L;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final int PERMISSION_REQUEST_CODE = 1000;
    private PadSdkHelper padSdkHelper;
    private List<BleDevice> mScanResultList;

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
    List<BleDevice> list = new ArrayList<>();
    private void initView() {
        findViewById(R.id.btn_search).setOnClickListener(v -> {
            checkPermissions();
            if (checkGPSIsOpen()){
                padSdkHelper.searchDevices(new BleScanCallback() {
                    @Override
                    public void onScanStarted(boolean success) {
                        // 开始扫描前
                        L.e("onScanStarted");
                        list.clear();
                    }

                    @Override
                    public void onLeScan(BleDevice bleDevice) {
                        super.onLeScan(bleDevice);
                        L.e("onLeScan"+(bleDevice == null));
                    }

                    @Override
                    public void onScanning(BleDevice bleDevice) {
                        //  扫描中-扫描到符合规则的设备
                        list.add(bleDevice);
                        L.e("onScanning"+(bleDevice == null));
                    }

                    @Override
                    public void onScanFinished(List<BleDevice> scanResultList) {
                        //  扫描结束-返回所有符合规则的设备
                        mScanResultList = scanResultList;
                        showScanResult(mScanResultList);
                        showScanResult(list);
                    }
                },"proximity","iMEMBER");
            }
        });
        findViewById(R.id.btn_conn).setOnClickListener(v -> {
            padSdkHelper.connect("macId");
//            padSdkHelper.connect(devices);
//            padSdkHelper.scanAndConnect();
        });
        findViewById(R.id.btn_dis_conn).setOnClickListener(v -> {
//            padSdkHelper.disconnect(devices);
            padSdkHelper.disconnectAllDevice();
        });
        findViewById(R.id.btn_all_devices).setOnClickListener(v -> {
            Set<BluetoothDevice> list = padSdkHelper.getConnectedDevices();
            //  获取已连接的全部设备信息
        });
    }

    private void showScanResult(List<BleDevice> list) {
        if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (BleDevice item : list) {
                sb.append("蓝牙名称:\t" + item.getName())
                        .append("\tmac地址:\t" + item.getMac())
                        .append("\trssi信号:\t" + item.getRssi())
                        .append("\tkey:\t" + item.getKey())
                        .append("\tdevices:\t" + item.getDevice().toString())
                        .append("\tscanRecord:\t" + item.getScanRecord().toString())
                        .append("\n");
            }
            TextView tv = findViewById(R.id.tv_result_list);
            tv.setText(sb.toString());
        }
    }
    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "蓝牙处于关闭状态，请打开蓝牙", Toast.LENGTH_LONG).show();
            padSdkHelper.onBlueTooth();
            return;
        }
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
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,

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
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
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