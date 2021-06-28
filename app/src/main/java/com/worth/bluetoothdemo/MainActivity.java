package com.worth.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.clj.fastble.data.BleDevice;
import com.espressif.iot.esptouch.EspHelper;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.bean.StateResult;
import com.worth.bluetooth.business.enter.PadSdkHelper;
import com.worth.bluetooth.business.utils.ParseHelper;
import com.worth.framework.base.core.utils.LDBus;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.CLICK;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.CONN_FAIL;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.CONN_OK;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.DIS_CONN;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.DOUBLE_CLICK;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.EVENT_TO_APP;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.PAIR_FAIL;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.PAIR_OK;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.PAIR_TIME_OUT;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.SCANNING;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.SCAN_FINISH;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.START_CONN;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.START_SCAN;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.STATION_RESULT;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.WIFI_INFO;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.WRITE_FAIL;
import static com.worth.bluetooth.business.gloable.PadToAppEventKeysKt.WRITE_OK;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final int PERMISSION_REQUEST_CODE = 1000;
    private PadSdkHelper padSdkHelper;
    private List<BleDevice> mScanResultList = new ArrayList<>();
    private BleDevice mBleDevice;
    private boolean scan = true;

    private Button search, conn, disConn, led, wifi;
    private EditText et1, et2, et3;
    private TextView tvWifiInfo, tvBluetoothList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();                               //  初始化依赖的权限
        initView();
        initListener();
        initObserver();                                 //  监听sdk错误的返回
        initSdk();                                      //  初始化sdk
    }

    private void initSdk() {
        padSdkHelper = PadSdkHelper.Companion.getInstance().initPadSdk();
    }

    private void initView() {

        wifi = findViewById(R.id.btn_wifi);
        conn = findViewById(R.id.btn_conn);
        search = findViewById(R.id.btn_search);
        led = findViewById(R.id.btn_control_led);
        disConn = findViewById(R.id.btn_dis_conn);

        et1 = findViewById(R.id.et_count);
        et2 = findViewById(R.id.et_interval);
        et3 = findViewById(R.id.et_wifi_password);

        tvWifiInfo = findViewById(R.id.tv_wifi_info);
        tvBluetoothList = findViewById(R.id.tv_result_list);
    }

    private void initListener() {
        search.setOnClickListener(v -> {
            checkBluetooth();
            if (checkGPSIsOpen()) {
                if (scan) {                             //  名称过滤只是第一步，决定返回与否蓝牙设备信息还是由广播解析出来的字段决定的
                    search.setText("取消扫描");
                    padSdkHelper.scanDevices(true,5000);//  调试期间debug为true
//                    padSdkHelper.scanDevices(5000, "proximity");
//                    padSdkHelper.scanDevices(5000, "proximity", "iMEMBER");
//                    padSdkHelper.scanDevices(5000, "proximity", "iMEMBER", "iStation");
                } else {
                    search.setText("开始扫描蓝牙设备");
                    padSdkHelper.cancelScan();
                }

                scan = !scan;
            }
        });

        conn.setOnClickListener(v -> {                  //  连接
            if (mBleDevice != null) {
                padSdkHelper.connect(mBleDevice);
            }
        });

        disConn.setOnClickListener(v -> {               //  断链
            if (mBleDevice != null) {
                padSdkHelper.disconnect(mBleDevice);
            }
        });

        led.setOnClickListener(v -> {                   //  控制led闪烁 可以输入次数和频率间隔
            if (mBleDevice != null) {
                int count = et1.getText().toString().isEmpty() ? 0 : Integer.parseInt(et1.getText().toString());
                int interval = et2.getText().toString().isEmpty() ? 0 : Integer.parseInt(et2.getText().toString());
                padSdkHelper.controlLed(mBleDevice, count, interval);
            }
        });

        wifi.setOnClickListener(v -> {
            if (isGetWifiFail) {
                LogHelper.toast("第一次获取wifi信息失败，请退出后再次进来尝试，或者切换个wifi", 200);

            } else {
                String password = et3.getText().toString().trim();
                if (!TextUtils.isEmpty(password)) {
                    // 此时可以loading展示--自行替换就可以了
                    LogHelper.toast("此时可以loading展示", 300);

                    EspHelper.INSTANCE.executeBroadcast(list -> {        //  获取到广播到的设备连接信息
                        // 此时可以loading销毁--自行替换就可以了
                        LogHelper.toast("此时可以结束loading", 400);

                        if (list == null) {
                            Log.e(TAG, "list == null");
                        } else {
                            for (IEsptouchResult item : list) {
                                if (item != null) {
                                    Log.e(TAG, "\t getBssid:" + item.getBssid()   //  设备的mac地址
                                            + "\t isSuc:" + item.isSuc()                //  任务是否执行成功
                                            + "\t isCancelled:" + item.isCancelled()    //  是否被用户手动取消了
                                    );

                                    //  ip地址--InetAddress里面还有很多信息，可以debug或者打印出来，看需要什么不？
                                    InetAddress address = item.getInetAddress();
                                    if (address != null) {
                                        Log.e(TAG, "address:" + address.toString());
                                    } else {
                                        Log.e(TAG, "address: == null");
                                    }
                                }
                            }
                        }
                        return null;
                    }, password);
                } else {
                    LogHelper.toast("wifi密码不可为空", 200);
                }
            }
        });
    }

    private boolean isGetWifiFail = true;

    private void initObserver() {
        LDBus.INSTANCE.observer2(EVENT_TO_APP, (eventKey, objectParams) -> {
            int key;
            if (eventKey != null && (key = (int) eventKey) > 0) {
                switch (key) {
                    case STATION_RESULT:                                                            //  扫描到基站信息-返回给app 状态信息2byte 产品id2byte mac地址6byte
                        if (objectParams != null) {
                            String state = ParseHelper.Companion.getInstance().getStateInfo(objectParams.toString());
                            String productId = ParseHelper.Companion.getInstance().getProductId(objectParams.toString());
                            String macId = ParseHelper.Companion.getInstance().getMacId(objectParams.toString());
                            String versionCode = ParseHelper.Companion.getInstance().getVersionCode(objectParams.toString());
                            boolean isCanConnectionOrPair = ParseHelper.Companion.getInstance().isCanConnectionOrPair(objectParams.toString());
                            boolean isFactoryState = ParseHelper.Companion.getInstance().isFactoryState(objectParams.toString());
                            boolean isContainEvent = ParseHelper.Companion.getInstance().isContainEvent(objectParams.toString());
                            Log.e(TAG, "状态信息--->2位：" + state);
                            Log.e(TAG, "产品Id--->2位：" + productId);
                            Log.e(TAG, "macId--->6位：" + macId);
                            Log.e(TAG, "版本号--->最大7.15：" + versionCode);
                            Log.e(TAG, "是否能被连接或配对--->：" + isCanConnectionOrPair);
                            Log.e(TAG, "是否是出厂状态，未配对--->：" + isFactoryState);
                            Log.e(TAG, "是否包含Event--->：" + isContainEvent);
                        }
                        break;
                    case WIFI_INFO:
                        if (objectParams != null && objectParams instanceof StateResult) {
                            StateResult wifiInfo = (StateResult) objectParams;
                            String mSsidByte = new String(wifiInfo.ssidBytes);
                            if (TextUtils.isEmpty(wifiInfo.ssid)) {
                                isGetWifiFail = true;
                                tvWifiInfo.setText("第一次获取wifi信息失败，请退出后再次进来尝试，或者切换个wifi");
                            } else {
                                isGetWifiFail = false;
                                String info = "\t wifi名称:" + wifiInfo.ssid
                                        + "\t wifi名称对应的byte，解析后:" + mSsidByte
                                        + "\t wifi的mac地址:" + wifiInfo.bssid
                                        + "\t message:" + wifiInfo.message
                                        + "\t is5G:" + wifiInfo.is5G
                                        + "\t address:" + wifiInfo.address
                                        + "\t wifiConnected:" + wifiInfo.wifiConnected;
                                tvWifiInfo.setText(info);
                            }
                        }
                        break;
                    case START_SCAN:                                                                //  开始扫描-做上次扫描数据清理工作
                        // 可做扫描的loading弹窗，但未连接情况下 是一直循环在扫描 所以不太合适！
                        break;
                    case SCANNING:                                                                  //  扫描中-可添加到自定义的list中 每次扫描到就展示到自定义的adapter中
                        if (objectParams != null && objectParams instanceof BleDevice) {
                            mBleDevice = (BleDevice) objectParams;
                        }
                        break;
                    case SCAN_FINISH:                                                               //  扫描结束-数据列表展示
                        //  可做dismiss 结束loading弹窗
                        if (objectParams != null) {
                            mScanResultList = (List<BleDevice>) objectParams;
                            showScanResult(mScanResultList);
                        } else {
                            showScanResult(null);
                        }
                        break;

                    case START_CONN:                                                                //  扫描结束后-开始连接某个设备
                        et1.setVisibility(View.GONE);
                        et2.setVisibility(View.GONE);
                        led.setVisibility(View.GONE);
                        conn.setText("蓝牙连接中...");
                        // 可做连接的loading弹窗
                        break;
                    case CONN_FAIL:                                                                 //  扫描结束后-连接设备失败
                        et1.setVisibility(View.GONE);
                        et2.setVisibility(View.GONE);
                        led.setVisibility(View.GONE);
                        conn.setText("蓝牙连接失败");
                        // 可做连接失败提示，并结束连接的loading弹窗
                        break;
                    case CONN_OK:                                                                   //  扫描结束后-连接设备成功
                        // 可做连接成功提示，并结束连接的loading弹窗
                        conn.setText("蓝牙连接成功");
                        et1.setVisibility(View.VISIBLE);
                        et2.setVisibility(View.VISIBLE);
                        led.setVisibility(View.VISIBLE);
                        break;
                    case DIS_CONN:                                                                  //  扫描结束后-在链接成功某个设备后，断开和某个设备的链接
                        et1.setVisibility(View.GONE);
                        et2.setVisibility(View.GONE);
                        led.setVisibility(View.GONE);
                        conn.setText("蓝牙连接");
                        disConn.setText("已断开蓝牙");
                        // 可做断开连接提示
                        break;

                    case CLICK:                                                                     //  会员卡单击事件回调
                        Log.e(TAG, "app-收到会员卡单击事件");
                        break;
                    case DOUBLE_CLICK:                                                              //  会员卡双击事件回调
                        Log.e(TAG, "app-收到会员卡双击事件");
                        break;

                    case WRITE_OK:                                                                  //  写入成功
                        Log.e(TAG, "app-收到写入会员卡数据成功事件");
                        break;
                    case WRITE_FAIL:                                                                //  写入失败
                        Log.e(TAG, "app-收到写入会员卡数据失败事件");
                        break;

                    case PAIR_OK:                                                                   //  配对成功
                        Log.e(TAG, "app-收到与会员卡配对成功事件");
                        break;
                    case PAIR_FAIL:                                                                 //  配对失败
                        Log.e(TAG, "app-收到与会员卡配对失败事件");
                        break;
                    case PAIR_TIME_OUT:                                                             //  配对超时
                        Log.e(TAG, "app-收到与会员卡配对超时事件");
                        break;

                }
            }
            return null;
        });

    }

    /**
     * test在界面上展示扫描到符合规则的列表结果
     */
    private void showScanResult(List<BleDevice> list) {
        if (list == null || list.size() == 0) {
            tvBluetoothList.setVisibility(View.GONE);
            tvBluetoothList.setText("");
        } else {
            tvBluetoothList.setVisibility(View.VISIBLE);
            tvBluetoothList.setText(LogHelper.printAndShowScanResult(list));
        }
    }

    /**
     * 检查蓝牙打开装填
     */
    private void checkBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            LogHelper.toast("蓝牙处于关闭状态，请打开蓝牙", 300);
            padSdkHelper.onOrOffBlueTooth(true);
            return;
        }
    }

    /***********************************************************************************************
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
     **********************************************************************************************/
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
}