package com.corporation8793.myapplication;

import android.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.corporation8793.myapplication.Thread.ConnectedThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.INTERNET;

public class MainActivity extends Activity {
    // global
    // TODO : 주소 수정 필요 !!
    final String HOST = "http://192.168.4.1";
    String DataType = "NONE";
    private final int PERMISSIONS_REQUEST_RESULT = 1;
    Map<String, String> extraHeaders = new HashMap<String, String>();

    // cw
    //CustomWebView wv;
    WebView wv;
    //CircleProgressView progress_circular;
    //float second = 20000;
    //float prg = 0;

    TextView tv_addressWithDataType;
    Button btn_stream, btn_bt, btn_status;


    BluetoothAdapter btAdapter;
    private final static int REQUEST_ENABLE_BT = 1;
    private String POOP_VALUE = "";

    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    ConnectedThread connectedThread = null;

    AlertDialog.Builder builder;
    ListView pair_lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();

        // permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, INTERNET)) {
                //권한을 거절하면 재 요청을 하는 함수
            } else {
                ActivityCompat.requestPermissions(this, new String[]{INTERNET}, PERMISSIONS_REQUEST_RESULT);
            }
        }



        // local
        wv = findViewById(R.id.wv);
        //progress_circular = findViewById(R.id.progress_circular);
        //progress_circular.setMaxValue(second);
        tv_addressWithDataType = findViewById(R.id.tv_addressWithDataType);
        btn_stream = findViewById(R.id.btn_stream);
        btn_bt = findViewById(R.id.btn_bt);
        btn_status = findViewById(R.id.btn_status);

        tv_addressWithDataType.setText("주소 : " + HOST + "\n" + "DataType : " + DataType);

        btn_stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.isSelected()) {
                    wv.stopLoading();
                    wv.loadUrl("about:blank");
                    setDataType("NONE");
                } else {
                    PlayHttpStream(HOST + ":81/stream");
                    setDataType("stream");
                    v.setSelected(true);
                }
            }
        });

        btn_bt.setOnClickListener(v -> {
            // 블루투스 코드 작성


            if(v.isSelected()) {
                Toast.makeText(this, "블루투스", Toast.LENGTH_SHORT).show();
                setDataType("NONE");
                if (btAdapter != null) {
                    if(connectedThread != null) {
                        connectedThread.write("E");
                    }
                }
//                    btAdapter.disable();


//                connectedThread.write("E");

            } else {


                if (btAdapter != null) {
                        pairing();


                        checking();

                    if(connectedThread != null) {
                        Log.e("write","a");
                        connectedThread.write("A");
                    }
                }else {

                    onBT();

                    // Pairing
                    pairing();


                    checking();
                }
                Toast.makeText(this, "블루투스", Toast.LENGTH_SHORT).show();
                setDataType("bt");
                v.setSelected(true);
//                connectedThread.write("W");
            }
        });

        btn_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.isSelected()) {
                    wv.stopLoading();
                    wv.loadUrl("about:blank");
                    setDataType("NONE");
                } else {
                    PlayHttpStream(HOST + "/status");
                    setDataType("status");
                    v.setSelected(true);
                }
            }
        });

        wv.setOnTouchListener((v, event) -> true);
    }

    //play http stream
    private void PlayHttpStream(String httpUrl){
        extraHeaders.put("X-Requested-With", "ok");

        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl(httpUrl, extraHeaders);
    }

    void setDataType(String str) {
        DataType = str;
        runOnUiThread(() -> {
            tv_addressWithDataType.setText("주소 : " + HOST + "\n" + "DataType : " + DataType);
            btn_stream.setSelected(false);
            btn_bt.setSelected(false);
            btn_status.setSelected(false);
        });
    }



    void checking() {
        // Check PMA, SOK
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                Log.e("btAdapter",btAdapter.getState()+"");
                if (true) {
                    if (connectedThread != null) {
                        if (connectedThread.getPMA()) {
                            Log.e("connect","...");
                            connectedThread.setPMA(false);
                        } else if (connectedThread.getSOK()) {

                            connectedThread.setSOK(false);
                        }
                    }
                    handler.postDelayed(this,1000);
                }
            }
        },1000);
    }

    void pairing() {
        builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.pairing, null);
        builder.setView(dialogView);

        pair_lv = dialogView.findViewById(R.id.pair_lv);

        // show paired devices
        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();

        btArrayAdapter.clear();
        if(deviceAddressArray!=null && !deviceAddressArray.isEmpty()){ deviceAddressArray.clear(); }
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
            }
        }

        pair_lv.setAdapter(btArrayAdapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        pair_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),"연결중입니다...",Toast.LENGTH_SHORT).show();
                dialog.cancel();

                final String name = btArrayAdapter.getItem(position); // get name
                final String address = deviceAddressArray.get(position); // get address
                boolean flag = true;

                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                BluetoothSocket btSocket = null;

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                // create & connect socket
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(uuid);
                    btSocket.connect();
                } catch (IOException e) {
                    flag = false;
                    Toast.makeText(getApplicationContext(), "connection failed!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                if(flag){
                    connectedThread = new ConnectedThread(btSocket);
                    connectedThread.start();

                    new Handler().postDelayed(() -> {
                        //딜레이 후 시작할 코드 작성
                        Toast.makeText(getApplicationContext(), "connected to " + name, Toast.LENGTH_SHORT).show();
                        connectedThread.write("A");
//                        dialog.cancel();
                    }, 1000);
                }
            }
        });
    }

    void onBT() {
        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    void getPermission() {
        // Get permission
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        ActivityCompat.requestPermissions(MainActivity.this, permission_list,  1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (btAdapter !=  null){
            if (connectedThread !=null){
                connectedThread.write("E");
                btAdapter.disable();
            }
        }

//        connectedThread.write("E");
    }
}