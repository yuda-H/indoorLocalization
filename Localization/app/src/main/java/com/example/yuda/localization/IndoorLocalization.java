package com.example.yuda.localization;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.List;

public class IndoorLocalization extends AppCompatActivity {
    TabHost tabHost;
    TabHost.TabSpec mTabSpec;
    WifiManager mWifiManager;
    TextView txt_timer,scanOrNot;
    Handler handler;

    @Override
    protected void onPause() {
        super.onPause();
        //Stop thread when you leave this app
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_localization);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        setTabHost();
        handler = new Handler();
    }


    public void setTabHost() {
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        mTabSpec = tabHost.newTabSpec("01");
        mTabSpec.setContent(R.id.tab1);
        mTabSpec.setIndicator("get info");
        tabHost.addTab(mTabSpec);

        mTabSpec = tabHost.newTabSpec("02");
        mTabSpec.setContent(R.id.tab2);
        mTabSpec.setIndicator("triangle counting");
        tabHost.addTab(mTabSpec);

    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String sss = "";
            mWifiManager.startScan();
            List<ScanResult> resultList = mWifiManager.getScanResults();
            try {
                for (int i = 0; i < resultList.size(); i++) {
                    ScanResult result = resultList.get(i);
                    sss +=  "\n" + result.SSID + "\n" + result.BSSID + "\b\b\b" + result.level + "\n";
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            txt_timer.setText(sss);
            handler.postDelayed(this, 1000);
        }
    };

    public void scan_start(View view) {
        txt_timer = (TextView)findViewById(R.id.txt_tab1);
        scanOrNot = (TextView)findViewById(R.id.txt_tab2);
        scanOrNot.setText("你已經開始掃描了");
        handler.post(runnable);



    }

    public void scan_stop(View view) {
        txt_timer = (TextView)findViewById(R.id.txt_tab1);
        scanOrNot = (TextView)findViewById(R.id.txt_tab2);
        scanOrNot.setText("你已經結束掃描了");
        handler.removeCallbacks(runnable);

    }
}
