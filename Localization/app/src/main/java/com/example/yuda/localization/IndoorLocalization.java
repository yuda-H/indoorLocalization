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
import java.util.Arrays;
import java.util.List;

public class IndoorLocalization extends AppCompatActivity {
    TabHost tabHost;
    TabHost.TabSpec mTabSpec;
    WifiManager mWifiManager;
    TextView txt_timer,scanOrNot,txt_baseInfo;
    Handler handler;
    String[][] base;
    String[] databae_BSSID;

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
        databae_BSSID = new String[]{"c8:3a:35:28:56:b0","00:a2:89:00:d9:61","1c:b7:2c:ed:b5:f8","c8:3a:35:14:ce:a0"};
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

    // scan wifi per second
    final Runnable runnable =




            new Runnable() {
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
            //wifiSorting(base);
            base = wifiChoosing(base);
            wifiSorting(base);
            txt_baseInfo.setText(Arrays.deepToString(base));
            handler.postDelayed(this, 1000);
        }
    };

    public void scan_start(View view) {
        txt_timer = (TextView)findViewById(R.id.txt_tab1);
        scanOrNot = (TextView)findViewById(R.id.txt_tab2);
        scanOrNot.setText("你正在掃描了");
        handler.post(runnable);
    }

    public void scan_stop(View view) {
        txt_timer = (TextView)findViewById(R.id.txt_tab1);
        scanOrNot = (TextView)findViewById(R.id.txt_tab2);
        scanOrNot.setText("你已經結束掃描了");
        handler.removeCallbacks(runnable);

    }

    // choosing wifi BSSID if the wifi of scanning is in your database
    public String[][] wifiChoosing(String[][] scanned) {
        String[] scan = new String[scanned.length];
        String[][] choosing_result = new String[databae_BSSID.length][2];
        for (int i=0; i<scanned.length; i++) {
            scan[i] = scanned[i][1];
        }
        List<String> scanList = Arrays.asList(scan);
        for (int i=0; i<databae_BSSID.length; i++) {
            if (scanList.contains(databae_BSSID[i])) {
                int k = scanList.indexOf(databae_BSSID[i]);
                choosing_result[i][0] = scanned[k][0];
                choosing_result[i][1] = scanned[k][1];
            }
            else {
                choosing_result[i][0] = -128+"";
                choosing_result[i][1] = databae_BSSID[i];
            }
        }
        return choosing_result;
    }

    // sorting wifi base on level
    public String[][] wifiSorting(String[][] chosen){
        for (int i=0; i<chosen.length-1 ;i++) {
            int index = i;
            int k=i;
            String change_level;
            String change_BSSID;
            for (int j=i+1; j<chosen.length; j++) {
                if(Double.parseDouble(chosen[j][0]) >= Double.parseDouble(chosen[k][0])) {
                    index = j;
                    k=index;
                }
            }
            change_level = chosen[i][0];
            chosen[i][0] = chosen[index][0];
            chosen[index][0] = change_level;
            change_BSSID = chosen[i][1];
            chosen[i][1] = chosen[index][1];
            chosen[index][1] = change_BSSID;
        }
        return chosen;
    }

    // triangle algorithm
    public double[][] coordinate(String[][] apBase, int distance) {

        double[][] a = new double[][]{};
        return a;
    }
}
