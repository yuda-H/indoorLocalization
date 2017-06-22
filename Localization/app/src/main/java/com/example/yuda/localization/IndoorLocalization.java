package com.example.yuda.localization;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class IndoorLocalization extends AppCompatActivity {
    TabHost tabHost;
    TabHost.TabSpec mTabSpec;
    WifiManager mWifiManager;
    TextView tvScanWifiResult,tvScanOrNot,tvBaseInfo, tvRecoding;
    Handler mHandler;
    String[][] aaryScanWifiInfo, aaryDatabaseInfo;
    FirebaseDatabase mdatabase;
    DatabaseReference mRef;
    Switch mSwitchForScanning;
    EditText etX, etY;
    jxlFile readBook, writeBook;
    String[][] choosing_result;
    boolean recoding=false;
    int recordingNumber = 15;
    int re = 3;
    int c;
    @Override
    protected void onPause() {
        super.onPause();
        //Stop thread when you leave this app
        mSwitchForScanning.setChecked(false);
        try {
            writeBook.Finish();
            readBook.workbook.close();
            writeBook.saveToReadbook(readBook,writeBook);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_localization);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mHandler = new Handler();
        mdatabase = FirebaseDatabase.getInstance();
        mRef = mdatabase.getReference("AP");
        mSwitchForScanning = (Switch)findViewById(R.id.swcScan);
        tvRecoding = (TextView)findViewById(R.id.tvRecoding);
        etX = (EditText)findViewById(R.id.etX);
        etY = (EditText)findViewById(R.id.etY);
        setTabHost();
        getDataListener();


        readBook = new jxlFile();
        try {
            readBook.setFolderPath("WifiDataSet");
            readBook.setFileName("wifiData.xls");
            readBook.getReadBook();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        writeBook = new jxlFile();
        try {
            writeBook.setFolderPath("WifiDataSet");
            writeBook.setFileName("C.xls");
            writeBook.setWriteBook();
            writeBook.copyCellsFromReadToWrite(readBook,readBook.workbook,writeBook.writableWorkbook);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }

    }


    public void btnSetCoordinate(View view) throws WriteException {
        String x = etX.getText().toString();
        String y = etY.getText().toString();

        int r = writeBook.writableSheet.getRows();
        c=r;
        if (choosing_result != null) {
            for (int i = 0; i < choosing_result.length; i++) {
                writeBook.setCell("null",0,r,0);
                writeBook.setCell("null",1,r,0);
                writeBook.setCell(choosing_result[i][0], 0, r, x);
                writeBook.setCell(choosing_result[i][0], 1, r, y);
                recoding = true;
            }
        }
    }

    public class jxlFile {

        WritableWorkbook writableWorkbook;
        Workbook workbook;
        File file;
        Sheet sheet;
        WritableSheet writableSheet;
        jxl.write.Label label;
        jxl.write.Number number;
        String folderPath, filePath;

        private void setFolderPath(String folderPath) {
            this.folderPath = Environment.getExternalStorageDirectory().getPath()+"/Documents/"+folderPath;
            file = new File(this.folderPath);
            if(!file.exists()) {
                file.mkdir();
            }
        }
        private void setFileName(String fileName) throws IOException, WriteException {
            this.filePath = folderPath+"/"+fileName;
            file = new File(this.filePath);
            if(!file.exists()) {
                setWriteBook();
                setSheet(" ");
                setCell(" ",0,0,"");
                Finish();
            }
        }
        private void getReadBook() throws IOException, BiffException, WriteException {
            this.workbook = workbook.getWorkbook(new File(this.filePath));
        }
        private void setWriteBook() throws IOException {
            this.writableWorkbook = Workbook.createWorkbook(new File(filePath));
        }
        private void setSheet(String sheetName) {
            writableSheet = writableWorkbook.createSheet(sheetName, 0);
        }
        private Sheet getSheet(String sheetName) {
            this.sheet = this.workbook.getSheet(textAdjusting(sheetName));
            return this.sheet;
        }
        private void copyCellsFromReadToWrite(jxlFile r,Workbook readBook, WritableWorkbook writeBook) throws WriteException, IOException, BiffException {
            int readSheetNumber = readBook.getNumberOfSheets();
            for (int i=0; i<readSheetNumber; i++) {
                writableSheet = writeBook.createSheet(readBook.getSheet(i).getName().toString(),0);
                for (int m=0; m<readBook.getSheet(i).getColumns(); m++) {
                    for (int n=0; n<readBook.getSheet(i).getRows(); n++) {
                        setCell(readBook.getSheet(i).getName().toString(),m,n,Double.parseDouble(r.getCell(readBook.getSheet(i).getName().toString(),m,n)));
                    }
                }
            }
        }
        private void setCell(String sheetName, int x,int y, String string) throws WriteException {
            label = new jxl.write.Label(x, y, string);
            writableSheet = writableWorkbook.getSheet(textAdjusting(sheetName));
            writableSheet.addCell(label);
        }
        private void setCell(String sheetName,int x,int y, int num) throws WriteException {
            number = new jxl.write.Number(x, y, num);
            writableSheet = writableWorkbook.getSheet(textAdjusting(sheetName));
            writableSheet.addCell(number);
        }
        private void setCell(String sheetName,int x,int y, double num) throws WriteException {
            number = new jxl.write.Number(x, y, num);
            writableSheet = writableWorkbook.getSheet(textAdjusting(sheetName));
            writableSheet.addCell(number);
        }
        private String getCell(String sheetName, int x, int y) {
            getSheet(sheetName);
            if(this.sheet.getCell(x,y).getContents()!=null) {
                return this.sheet.getCell(x,y).getContents();
            } else {
                return "null";
            }

            //return this.sheet.getCell(x,y).getContents()+"";
        }
        private void saveToReadbook(jxlFile readBook, jxlFile writeBook) throws IOException, BiffException, WriteException {
            writeBook.getReadBook();
            readBook.writableWorkbook = Workbook.createWorkbook(new File(readBook.filePath),writeBook.workbook);
            readBook.Finish();
        }
        private void Finish() throws IOException, WriteException {
            writableWorkbook.write();
            writableWorkbook.close();
        }
    }

    // set Tab
    public void setTabHost() {
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        mTabSpec = tabHost.newTabSpec("tab01");
        mTabSpec.setContent(R.id.tab1);
        mTabSpec.setIndicator("get info");
        tabHost.addTab(mTabSpec);

        mTabSpec = tabHost.newTabSpec("tab02");
        mTabSpec.setContent(R.id.tab2);
        mTabSpec.setIndicator("triangle counting");
        tabHost.addTab(mTabSpec);

        mTabSpec = tabHost.newTabSpec("tab03");
        mTabSpec.setContent(R.id.tab3);
        mTabSpec.setIndicator("test");
        tabHost.addTab(mTabSpec);
    }

    // control on or off for wifi scanning
    public void mSwitch(View view) {
        if (mSwitchForScanning.isChecked()) {
            tvScanWifiResult = (TextView)findViewById(R.id.txtScanWifiResult);
            tvScanOrNot = (TextView)findViewById(R.id.txt_tab2);
            tvScanOrNot.setText("你正在掃描了");

            if (Arrays.deepToString(aaryDatabaseInfo).equals("null")) {
                tvScanWifiResult.setText("請確認您有連上網路以下載所需資料，並重新開始掃描");
                mSwitchForScanning.setChecked(false);
            } else {
                mHandler.post(runnable);
            }
        } else {
            tvScanWifiResult = (TextView)findViewById(R.id.txtScanWifiResult);
            tvScanOrNot = (TextView)findViewById(R.id.txt_tab2);
            tvScanOrNot.setText("你已經結束掃描了");
            mHandler.removeCallbacks(runnable);
            tvScanWifiResult.setText("");
        }
    }

    // get wifi info on firebase database(include BSSID, RSSID-max, x-coordinate, y-coordinate)
    public void getDataListener() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                aaryDatabaseInfo = new String[(int)dataSnapshot.getChildrenCount()][4];
                // {[BSSID, x, y]}
                for (int i=1; i<=aaryDatabaseInfo.length; i++) {
                    for (int j=0; j<aaryDatabaseInfo[0].length; j++) {
                        switch (j) {
                            case 0: aaryDatabaseInfo[i-1][j] = dataSnapshot.child("AP"+i).child("BSSID").getValue()+"";
                                if (readBook.getSheet(aaryDatabaseInfo[i-1][j]) == null) {
                                    writeBook.setSheet(aaryDatabaseInfo[i-1][j]);
                                }
                                break;
                            case 1: aaryDatabaseInfo[i-1][j] = dataSnapshot.child("AP"+i).child("RSSI_MAX").getValue()+"";
                                break;
                            case 2: aaryDatabaseInfo[i-1][j] = dataSnapshot.child("AP"+i).child("x").getValue()+"";
                                break;
                            case 3: aaryDatabaseInfo[i-1][j] = dataSnapshot.child("AP"+i).child("y").getValue()+"";
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
        choosing_result = new String[aaryDatabaseInfo.length][5];
        //[BSSID, scan_RSSI, data_RSSI, data_x, data_y]
        for (int i=0; i<scanned.length; i++) {                  //把scanned(2D)的BSSID抓到scanList
            scan[i] = scanned[i][0];
        }
        List<String> scanList = Arrays.asList(scan);
        for (int i=0; i<aaryDatabaseInfo.length; i++) {
            if (scanList.contains(aaryDatabaseInfo[i][0])) {           //如果從掃描的結果中有包含database的第i個  i=0 1 2 ...
                int k = scanList.indexOf(aaryDatabaseInfo[i][0]);      //則取得scanList與第i個BSSID一樣的index(也會等於scanned的index)
                choosing_result[i][0] = aaryDatabaseInfo[i][0];        //結果的第0個位置放database的第i個BSSID
                choosing_result[i][1] = scanned[k][1];                 //結果的第1個位置放當前的RSSI 也就是scanned[k]
                choosing_result[i][2] = aaryDatabaseInfo[i][1];        //結果的第2個位置放database的第i個RSSI_MAX
                choosing_result[i][3] = aaryDatabaseInfo[i][2];        //結果的第3個位置放database的第i個RSSI的x
                choosing_result[i][4] = aaryDatabaseInfo[i][3];        //結果的第4個位置放database的第i個RSSI的y
            }
            else {
                choosing_result[i][0] = aaryDatabaseInfo[i][0];
                choosing_result[i][1] = -128+"";
                choosing_result[i][2] = aaryDatabaseInfo[i][1];
                choosing_result[i][3] = aaryDatabaseInfo[i][2];
                choosing_result[i][4] = aaryDatabaseInfo[i][3];
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

    // Adjust ':' to '@' when you call getCell
    public String textAdjusting(String string) {
        char aryString[] = new char[string.length()];
        for (int i=0; i<string.length(); i++) {
            if (string.charAt(i) == ':') {
                aryString[i] = '@';
            } else {
                aryString[i] = string.charAt(i);
            }
        }
        return String.valueOf(aryString);
    }

    // scan wifi per second
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder sss = new StringBuilder();
            sss.append("");
            mWifiManager.startScan();
            List<ScanResult> resultList = mWifiManager.getScanResults();
            tvBaseInfo = (TextView)findViewById(R.id.txt_baseInfo);
            aaryScanWifiInfo = new String[resultList.size()][];

            try {
                for (int i = 0; i < resultList.size(); i++) {
                    ScanResult result = resultList.get(i);
                    aaryScanWifiInfo[i] = new String[2];
                    aaryScanWifiInfo[i][0] = result.BSSID;
                    aaryScanWifiInfo[i][1] = result.level+"";

                    sss.append("\n").append(result.SSID).append("\n").append(result.BSSID).append("\b\b\b").append(result.level).append("\n");
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
            tvScanWifiResult.setText(sss);
            aaryScanWifiInfo = wifiChoosing(aaryScanWifiInfo);
            //wifiSorting(aaryScanWifiInfo);
            printArray(aaryScanWifiInfo);

            if(recoding == true && re<=recordingNumber+2) {
                try {
                    writeBook.setCell("null",re-1,c,1);
                    tvRecoding.setText("紀錄中...正在紀錄第 "+(re-2)+" 項，還剩下 "+(recordingNumber-re+2)+" 項");
                    for (int i=0; i<aaryScanWifiInfo.length; i++) {
                        writeBook.setCell(aaryScanWifiInfo[i][0],re-1,c,aaryScanWifiInfo[i][1]);
                    }
                    re++;
                } catch (WriteException e) {
                    e.printStackTrace();
                }
            } else {
                recoding = false;
                re = 3;
                tvRecoding.setText("紀錄已完成");
            }

            mHandler.postDelayed(this, 3500);
        }
    };

    public void printArray(String[][] arr) {
        StringBuilder text = new StringBuilder();
        text.append("BSSID                       \tscanRSSI\tdataRSSI\t\t\tx\t\t\t\ty\n");
        for (String[] anArr : arr) {
            for (int j = 0; j < arr[0].length; j++) {
                text.append(anArr[j]).append("\t\t\t");
            }
            text.append("\n");
        }
        tvBaseInfo.setText(text);
    }

}
