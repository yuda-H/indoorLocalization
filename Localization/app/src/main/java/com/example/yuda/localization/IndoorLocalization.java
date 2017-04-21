package com.example.yuda.localization;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

public class IndoorLocalization extends AppCompatActivity {
    TabHost tabHost;
    TabHost.TabSpec mTabSpec;
    WifiManager mWifiManager;
    TextView txt_timer,scanOrNot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        setContentView(R.layout.activity_indoor_localization);
        setTabHost();
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

    public void scan_start(View view) {
        scanOrNot = (TextView)findViewById(R.id.txt_tab2);
        scanOrNot.setText("你已經開始掃描了");
    }

    public void scan_stop(View view) {
        scanOrNot = (TextView)findViewById(R.id.txt_tab2);
        scanOrNot.setText("你已經結束掃描了");
    }
}
