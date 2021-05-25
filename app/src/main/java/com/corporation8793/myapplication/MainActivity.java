package com.corporation8793.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button wifi_connect, bluetooth_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        wifi_connect = findViewById(R.id.wifi_connect);
        bluetooth_connect = findViewById(R.id.bluetooth_connect);


        wifi_connect.setOnClickListener(v -> {

        });

        bluetooth_connect.setOnClickListener(v -> {

        });

    }
}