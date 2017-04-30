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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class IndoorLocalization extends AppCompatActivity {
    TabHost tabHost;
    TabHost.TabSpec mTabSpec;
    WifiManager mWifiManager;
    TextView txt_timer,scanOrNot,txt_baseInfo;
    Handler handler;
    String[][] base, dataArray;
    String[] databae_BSSID;
    FirebaseDatabase mdatabase;
    DatabaseReference mRef;

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
        mdatabase = FirebaseDatabase.getInstance();
        mRef = mdatabase.getReference("AP");
        getDataListener();
        databae_BSSID = new String[]{"c8:3a:35:28:56:b0"};
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
        txt_timer = (TextView)findViewById(R.id.txt_tab1);
        scanOrNot = (TextView)findViewById(R.id.txt_tab2);
        scanOrNot.setText("你正在掃描了");
        if (Arrays.deepToString(dataArray).equals("null")) {
            txt_timer.setText("請確認您有連上網路以下載所需資料，並重新開始掃描");
        } else {
            handler.post(runnable);
        }
    }

    public void scan_stop(View view) {
        txt_timer = (TextView)findViewById(R.id.txt_tab1);
        scanOrNot = (TextView)findViewById(R.id.txt_tab2);
        scanOrNot.setText("你已經結束掃描了");
        handler.removeCallbacks(runnable);

    }

    public void getDataListener() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataArray = new String[(int)dataSnapshot.getChildrenCount()][4];
                // {[BSSID, x, y]}
                for (int i=1; i<=dataArray.length; i++) {
                    for (int j=0; j<dataArray[0].length; j++) {
                        switch (j) {
                            case 0: dataArray[i-1][j] = dataSnapshot.child("AP"+i).child("BSSID").getValue()+"";
                                break;
                            case 1: dataArray[i-1][j] = dataSnapshot.child("AP"+i).child("RSSI_MAX").getValue()+"";
                                break;
                            case 2: dataArray[i-1][j] = dataSnapshot.child("AP"+i).child("x").getValue()+"";
                                break;
                            case 3: dataArray[i-1][j] = dataSnapshot.child("AP"+i).child("y").getValue()+"";
                                break;

                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // choosing wifi BSSID if the wifi of scanning is in your database
    public String[][] wifiChoosing(String[][] scanned) {
        String[] scan = new String[scanned.length];
        String[][] choosing_result = new String[dataArray.length][5];
        //[BSSID, scan_RSSI, data_RSSI, data_x, data_y]
        for (int i=0; i<scanned.length; i++) {                  //把scanned(2D)的BSSID抓到scanList
            scan[i] = scanned[i][0];
        }
        List<String> scanList = Arrays.asList(scan);
        for (int i=0; i<dataArray.length; i++) {
            if (scanList.contains(dataArray[i][0])) {           //如果從掃描的結果中有包含database的第i個  i=0 1 2 ...
                int k = scanList.indexOf(dataArray[i][0]);      //則取得scanList與第i個BSSID一樣的index(也會等於scanned的index)
                choosing_result[i][0] = dataArray[i][0];        //結果的第0個位置放database的第i個BSSID
                choosing_result[i][1] = scanned[k][1];          //結果的第1個位置放當前的RSSI 也就是scanned[k]
                choosing_result[i][2] = dataArray[i][1];        //結果的第2個位置放database的第i個RSSI_MAX
                choosing_result[i][3] = dataArray[i][2];        //結果的第3個位置放database的第i個RSSI的x
                choosing_result[i][4] = dataArray[i][3];        //結果的第4個位置放database的第i個RSSI的y
            }
            else {
                choosing_result[i][0] = dataArray[i][0];
                choosing_result[i][1] = -128+"";
                choosing_result[i][2] = dataArray[i][1];
                choosing_result[i][3] = dataArray[i][2];
                choosing_result[i][4] = dataArray[i][3];
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
                if(Double.parseDouble(chosen[j][1]) >= Double.parseDouble(chosen[k][1])) {
                    index = j;
                    k=index;
                }
            }
            change_BSSID = chosen[i][0];
            chosen[i][0] = chosen[index][0];
            chosen[index][0] = change_BSSID;
            change_level = chosen[i][1];
            chosen[i][1] = chosen[index][1];
            chosen[index][1] = change_level;

        }
        return chosen;
    }

    // triangle algorithm, [rssi, BSSID, coordinate_x, coordinate_y] has been chosen and sorted
    public String[][] coordinate(String[][] apBase, int distance) {
        double[] x = new double[apBase.length];
        double[] y = new double[apBase.length];

        return apBase;
    }

    // scan wifi per second
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
                    base[i][0] = result.BSSID;
                    base[i][1] = result.level+"";

                    sss +=  "\n" + result.SSID + "\n" + result.BSSID + "\b\b\b" + result.level + "\n";
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            txt_timer.setText(sss);
            base = wifiChoosing(base);
            wifiSorting(base);
            txt_baseInfo.setText(Arrays.deepToString(base)+"\n\n\n\n\n\n"+Arrays.deepToString(dataArray));
            handler.postDelayed(this, 500);
        }
    };
}
