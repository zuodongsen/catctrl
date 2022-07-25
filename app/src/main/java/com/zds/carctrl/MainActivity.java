package com.zds.carctrl;

import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;

import java.net.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.List;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class MainActivity extends AppCompatActivity {
    private Socket clientSocket;
    private Socket client;

    private Thread clientThread;

    private BufferedReader clientReader;
    private PrintWriter clientWriter;

    private int scanWifiTimes;

    TextView txtInfo;

    Button btn_slow;
    Button btn_fast;
    Button connect;

    Button btn_forvart;
    Button btn_back;
    Button btn_left;
    Button btn_right;
    Button btn_stop;

    private String ipAdd;
    private int port;

    private boolean isConnecting;

    private String clientStr;
    private static WifiManager mWifiManager;
    private ButtonDownUpListener btnDUListener;
    private Thread scanWifiThread;
    private Thread checkWifiThread;
    private IpScan ipScan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        ipAdd = "10.10.100.254";
        port = 8899;
        scanWifiTimes = 1;

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        initText();
        initButtons();
        setAllButtonState(false);
        initWifiMng();
        initThreads();

    }

    void initText() {
        ipScan = new IpScan();
        txtInfo = (TextView) findViewById(R.id.textInfo);
        txtInfo.setText(txtInfo.getText() + ipScan.getLocalIp());
//        ipScan.scan();
    }

    void initThreads() {
        scanWifiThread = new Thread(scanfRun);
        checkWifiThread = new Thread(timeerRunable);
        clientThread = new Thread(ClientRunnable);
        scanWifiThread.start();
    }

    void initWifiMng() {
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    }

    void initButtons() {
        btn_slow = (Button) findViewById(R.id.slow);
        btn_fast = (Button) findViewById(R.id.fast);
        connect = (Button) findViewById(R.id.connect);

        btn_forvart = (Button) findViewById(R.id.forvard);
        btn_back = (Button) findViewById(R.id.back);
        btn_left = (Button) findViewById(R.id.left);
        btn_right = (Button) findViewById(R.id.right);
        btn_stop = (Button) findViewById(R.id.stop);

        btnDUListener = new ButtonDownUpListener();
        btnDUListener.clientWriter = this.clientWriter;
        btn_forvart.setOnTouchListener(btnDUListener);
        btn_back.setOnTouchListener(btnDUListener);
        btn_left.setOnTouchListener(btnDUListener);
        btn_right.setOnTouchListener(btnDUListener);
        btn_stop.setOnTouchListener(btnDUListener);
    }

    void setAllButtonState(boolean isValid) {
        btn_slow.setEnabled(isValid);
        btn_fast.setEnabled(isValid);
        connect.setEnabled(isValid);
        btn_forvart.setEnabled(isValid);
        btn_back.setEnabled(isValid);
        btn_left.setEnabled(isValid);
        btn_right.setEnabled(isValid);
        btn_stop.setEnabled(isValid);
    }

    void threadSleep(long ms) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //程序启动自动搜索“smartCar”
    private Runnable scanfRun = new Runnable() {
        @Override
        public void run() {
            System.out.println("scanfRun");
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while(true) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID().replace("\"", "").replace("<", "").replace(">", "");;

                System.out.println(ssid.indexOf("Bee"));
                if(ssid.indexOf("Bee") == 0) {
                    System.out.println("get bee");
                    Message mes = new Message();
                    mes.what = 0x12;
                    myHandler.sendMessage(mes);
                    return;
                }
                System.out.println(ssid);
                threadSleep(40000);

            }

        }
    };



    //定时器可执行代码
    private Runnable timeerRunable = new Runnable() {
        @Override
        public void run() {
            while(true) {
                threadSleep(20000);
                Message mes = new Message();
                mes.what = 0x14;
                myHandler.sendMessage(mes);
            }
        }
    };


    @SuppressLint("HandlerLeak")
    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x11) {
                TextView txt = (TextView)findViewById(R.id.textInfo);
                btnDUListener.clientWriter = (PrintWriter) msg.obj;
                txt.setText("连接成功");
                setAllButtonState(true);
                connect.setEnabled(false);
                checkWifiThread.start();
            } else if(msg.what == 0x12) {
                TextView txt = (TextView)findViewById(R.id.textInfo);
                txt.setText("已找到smartCar，开始链接可用");
                connect.setEnabled(true);

            } else if(msg.what == 0x13) {
                TextView txt = (TextView)findViewById(R.id.textInfo);
                System.out.println("rcv ox13");
                txt.setText("扫描" + scanWifiTimes + "次，未找到smartCar");
                scanWifiTimes ++;
            } else if(msg.what == 0x14) {//检测链接状态
                System.out.println("rcv ox14");
                if(mWifiManager.isWifiEnabled()) {
                    int a1 = WifiManager.WIFI_STATE_DISABLED;
                    int a2 = WifiManager.WIFI_STATE_DISABLING;
                    int a3 = WifiManager.WIFI_STATE_ENABLED;
                    int a4 = WifiManager.WIFI_STATE_ENABLING;
                    int a5 = WifiManager.WIFI_STATE_UNKNOWN;

                    int a = mWifiManager.getWifiState();


                    WifiInfo info = mWifiManager.getConnectionInfo();
                    if(info != null) {
                        String ssid = info.getSSID();
                        if(ssid.contains("Bee")) {
                            return;
                        }
                    }
                }
                setAllButtonState(false);
            }
        }

    };

    ///按钮电机事件
    public void btn_1Onclick(View view) {
        Button btn = (Button)view;
        int a = btn.getId();

        String text = btn.getText().toString();

        if(text.contains("链接")) {
            if(!isConnecting) {
                isConnecting = true;
                clientThread.start();
            }
        } else if(text.contains("加速")) {
            if(this.clientWriter != null) {
                this.clientWriter.print("FFfast:100EE");
                this.clientWriter.flush();
            }
        } else if(text.contains("减速")) {
            if(this.clientWriter != null) {
                this.clientWriter.print("FFslow:0EE");
                this.clientWriter.flush();
            }
        } else if(text.contains("")) {

        }

    }

    private Runnable ClientRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                clientSocket = new Socket(ipAdd, port);
                clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientWriter = new PrintWriter(clientSocket.getOutputStream());

                Message mse = new Message();
                mse.what = 0x11;
                mse.obj = clientWriter;
                clientStr = "连接成功";

                myHandler.sendMessage(mse);

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    };

}