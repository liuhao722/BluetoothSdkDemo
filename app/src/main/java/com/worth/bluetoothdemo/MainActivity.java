package com.worth.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.clj.fastble.data.BleDevice;
import com.clj.fastble.utils.HexUtil;
import com.worth.bluetooth.business.enter.PadSdkHelper;
import com.worth.framework.base.core.utils.LDBus;

import java.util.ArrayList;
import java.util.List;

import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_CONNECTION_FAIL;
import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_CONNECTION_SUCCESS;
import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_DIS_CONNECTION;
import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_SCANNING;
import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_SCAN_FINISH;
import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_START_CONNECTION;
import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_START_SCAN;
import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_TO_APP_KEY;
import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_TYPE_CLICK;
import static com.worth.bluetooth.business.gloable.PadSdkConstantKt.EVENT_TYPE_DOUBLE_CLICK;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final int PERMISSION_REQUEST_CODE = 1000;
    private PadSdkHelper padSdkHelper;
    private List<BleDevice> mScanResultList = new ArrayList<>();
    private BleDevice mBleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();       //  初始化依赖的权限
        initView();
        initSdk();              //  初始化sdk
        initObserver();         //  监听sdk错误的返回
    }

    private void initSdk() {
        padSdkHelper = PadSdkHelper.Companion.getInstance().initPadSdk();
    }


    private void initObserver() {
        LDBus.INSTANCE.observer2(EVENT_TO_APP_KEY, (eventKey, objectParams) -> {
            int key;
            if (eventKey != null && (key = (int) eventKey) > 0) {
                switch (key) {
                    case EVENT_START_SCAN:                                                          //  开始扫描-做上次扫描数据清理工作
                        // 可做loading弹窗
                        break;
                    case EVENT_SCANNING:                                                            //  扫描中-可添加到自定义的list中 每次扫描到就展示到自定义的adapter中
                        if (objectParams != null && objectParams instanceof BleDevice) {
                            mBleDevice = (BleDevice) objectParams;
                        }
                        break;
                    case EVENT_SCAN_FINISH:                                                         //  扫描结束-数据列表展示
                        //  可做dismiss 结束loading弹窗
                        if (objectParams != null) {
                            mScanResultList = (List<BleDevice>) objectParams;
                            showScanResult(mScanResultList);
                        }
                        break;

                    case EVENT_START_CONNECTION:                                                    //  扫描结束后-开始连接某个设备
                        // 可做连接的loading弹窗
                        break;
                    case EVENT_CONNECTION_FAIL:                                                     //  扫描结束后-连接设备失败
                        // 可做连接失败提示，并结束连接的loading弹窗
                        break;
                    case EVENT_CONNECTION_SUCCESS:                                                  //  扫描结束后-连接设备成功
                        // 可做连接成功提示，并结束连接的loading弹窗
                        break;
                    case EVENT_DIS_CONNECTION:                                                      //  扫描结束后-在链接成功某个设备后，断开和某个设备的链接
                        // 可做断开连接提示
                        break;

                    case EVENT_TYPE_CLICK:
                        break;
                    case EVENT_TYPE_DOUBLE_CLICK:
                        break;
                }
            }
            return null;
        });

    }

    private void initView() {
        findViewById(R.id.btn_search).setOnClickListener(v -> {
            checkPermissions();
            if (checkGPSIsOpen()) {
                padSdkHelper.scanDevices(5000);
            }
        });

        findViewById(R.id.btn_conn).setOnClickListener(v -> {
            if (mBleDevice != null) {
                padSdkHelper.connect(mBleDevice);
            }
        });

        findViewById(R.id.btn_dis_conn).setOnClickListener(v -> {
            if (mBleDevice != null) {
                padSdkHelper.disconnect(mBleDevice);
            }
        });

        findViewById(R.id.btn_all_devices).setOnClickListener(v -> {
            List<BleDevice> list = padSdkHelper.getConnectedDevices();                              //  获取已连接的全部设备信息
            LogHelper.printAndShowScanResult(list);
        });
    }

    private void showScanResult(List<BleDevice> list) {
        TextView tv = findViewById(R.id.tv_result_list);
        tv.setText(LogHelper.printAndShowScanResult(list));
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
//            Toast.makeText(this, "蓝牙处于关闭状态，请打开蓝牙", Toast.LENGTH_LONG).show();
            padSdkHelper.onBlueTooth();
            return;
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     * <!--    蓝牙所需权限，可能扫描时候还需要一个位置-->
     * <uses-permission android:name="android.permission.BLUETOOTH" />
     * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
     * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     * <p>
     * <!--    智能设备所需权限 sdk需要的-->
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
     * <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
     * <p>
     * <p>
     * <!--    基站app需要的-->
     * <uses-permission android:name="android.permission.INTERNET" />
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,

                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
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
        padSdkHelper.release();
    }

    public void show(BleDevice bleDevice, int count, int intervalTime) {
        int time1 = 1000;
        int time2 = 6000;
        if (intervalTime * count * 2 > 65535) {
            time2 = 65535;
            time1 = 65535 / 2 / count;
        }
        byte[] result = HexUtil.hexStringToBytes("023004" + Integer.toHexString(time1) + Integer.toHexString(time2));
    }
}