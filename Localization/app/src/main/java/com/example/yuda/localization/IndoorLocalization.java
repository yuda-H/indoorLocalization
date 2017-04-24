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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndoorLocalization extends AppCompatActivity {
    TabHost tabHost;
    TabHost.TabSpec mTabSpec;
    WifiManager mWifiManager;
    TextView txt_timer,scanOrNot,txt_baseInfo;
    Handler handler;
    String[][] base;

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
            txt_baseInfo = (TextView)findViewById(R.id.txt_baseInfo);
            base = new String[resultList.size()][];
            try {
                for (int i = 0; i < resultList.size(); i++) {
                    ScanResult result = resultList.get(i);
                    base[i] = new String[2];
                    base[i][0] = result.level+"";
                    base[i][1] = result.BSSID;
                    sss +=  "\n" + result.SSID + "\n" + result.BSSID + "\b\b\b" + result.level + "\n";
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            txt_timer.setText(sss);
            base = wifiSorting(base);
            txt_baseInfo.setText(Arrays.deepToString(base));
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

    public String[][] wifiSorting(String[][] base){
        for (int i=0; i<base.length-1 ;i++) {
            int index = i;
            int k=i;
            String change_level;
            String change_BSSID;
            for (int j=i+1; j<base.length; j++) {
                if(Double.parseDouble(base[j][0]) >= Double.parseDouble(base[k][0])) {
                    index = j;
                    k=index;
                }
            }
            change_level = base[i][0];
            base[i][0] = base[index][0];
            base[index][0] = change_level;
            change_BSSID = base[i][1];
            base[i][1] = base[index][1];
            base[index][1] = change_BSSID;
        }
        return base;
    }

    public ArrayList<Double> trianglePoint1(double x, double y, double length) {
        ArrayList<Double> aa = new ArrayList<>();
        return aa;
    }
}
