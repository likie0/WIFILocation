package com.example.wifilocation;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = "Y30J";
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1000;
    private final int UPDATE_UI_REQUEST_CODE = 1024;
    private final String BASE0 = "lirex758002";
    private final String BASE1 = "HUAWEI-SENSOR";
    private final String BASE2 = "CMCC-FCfP";
    private final String BASE3 = "TP-LINK_A494";
    private final String BASE4 = "203";

    private TextView mScanResultTV;    // 显示WiFi扫描结果的控件
    private Button scanButton;
    private FingerPrint fingerPrint;
    private StringBuffer mScanResultStr;    // 暂存WiFi扫描结果的字符串
    private WifiManager mWifiManager;   // 调用WiFi各种API的对象
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_UI_REQUEST_CODE) {
                updateUI();
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        getLocationAccessPermission();  // 先获取位置权限
        Log.d(TAG, "onCreate: ");

        TextView tv2 = findViewById(R.id.tv2);
        tv2.setTextColor(Color.BLUE);

        mScanResultTV = findViewById(R.id.scan_results_info_tv);
        scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ");
                scanWifi();
                mHandler.sendEmptyMessage(UPDATE_UI_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * 增加开启位置权限功能，以适应Android 6.0及以上的版本
     */
    private void getLocationAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
        }
    }

    public void scanWifi() {
        // 如果WiFi未打开，先打开WiFi
        if (!mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(true);

        // 开始扫描WiFi
        mWifiManager.startScan();
        // 获取并保存WiFi扫描结果
        Log.d(TAG, "scanWifi: ");
        mScanResultStr = new StringBuffer();
        fingerPrint = new FingerPrint(0, 0, -150, -150, -150, -150, -150);
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        for (ScanResult sr : scanResults) {
            switch (sr.SSID) {
                case BASE0:
                    fingerPrint.setSs1(sr.level);
                    break;
                case BASE1:
                    fingerPrint.setSs2(sr.level);
                    break;
                case BASE2:
                    fingerPrint.setSs3(sr.level);
                    break;
                case BASE3:
                    fingerPrint.setSs4(sr.level);
                    break;
                case BASE4:
                    fingerPrint.setSs5(sr.level);
                    break;
            }
            mScanResultStr.append("SSID: ").append(sr.SSID).append("\n");
            mScanResultStr.append("MAC Address: ").append(sr.BSSID).append("\n");
            mScanResultStr.append("Signal Strength(dBm): ").append(sr.level).append("\n\n");
            Log.d(TAG, "SSID:" + sr.SSID + "  MAC Address: " + sr.BSSID + "  Signal Strength:" + sr.level);

            // TODO 将结果存入数据库
        }
        Log.d(TAG, "RESULT: " + fingerPrint.getSs1() + "  " + fingerPrint.getSs2() + "  " + fingerPrint.getSs3() + "  " + fingerPrint.getSs4() + "  " + fingerPrint.getSs5());
    }

    private void updateUI() {
        Log.d(TAG, "updateUI: ");
        mScanResultTV.setText(mScanResultStr);
    }
}