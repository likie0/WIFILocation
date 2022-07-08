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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wifilocation.util.OKHttpUtil;
import com.google.gson.Gson;

import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = "Gunp";
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1000;
    private final int UPDATE_UI_REQUEST_CODE = 1024;

    //根据自己家附近的AP调整MAC地址
    private final String BASE0 = "1c:b7:96:24:bc:68";//lirex758002
    private final String BASE1 = "1c:b7:96:24:bc:6c";//HUAWEI-SENSOR
    private final String BASE2 = "28:23:f5:47:db:68";//CMCC-FCfp
    private final String BASE3 = "78:44:fd:b1:a4:94";//TP-LINK_A494
    private final String BASE4 = "a0:d9:3d:ef:cd:d0";//203

    //这里是访问地址
    private final String baseUrl = "https://h5556095v9.zicp.fun";

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
    private EditText et_y;
    private EditText et_x;


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
        et_x = findViewById(R.id.et_x);
        et_y = findViewById(R.id.et_y);
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
        fingerPrint = new FingerPrint(0, 0, -100, -100, -100, -100, -100);
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        for (ScanResult sr : scanResults) {
            switch (sr.BSSID) {
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
        }

        // 坐标保存
        int X = Integer.parseInt(et_x.getText().toString());
        int Y = Integer.parseInt(et_y.getText().toString());
        fingerPrint.setPositionX(X);
        fingerPrint.setPositionY(Y);

        //将封装好的对象发送给服务器
        Gson gson = new Gson();
        String json = gson.toJson(fingerPrint);
        String args[] = new String[]{"collection", "collection"};
        try {
            String res = OKHttpUtil.postSyncRequest(baseUrl, json, args);
            Log.d("服务器返回值:", res);
        } catch (Exception e) {
            //连接失败
            Toast.makeText(this, "请检查网络连接", Toast.LENGTH_SHORT).show();
        }


        Log.d(TAG, "RESULT: " + fingerPrint.getSs1() + "  " + fingerPrint.getSs2() + "  " + fingerPrint.getSs3() + "  " + fingerPrint.getSs4() + "  " + fingerPrint.getSs5());
    }

    private void updateUI() {
        Log.d(TAG, "updateUI: ");
        mScanResultTV.setText(mScanResultStr);
    }
}