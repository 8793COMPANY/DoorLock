package com.corporation8793.myapplication;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            } else {
                Toast.makeText(this, "블루투스", Toast.LENGTH_SHORT).show();
                setDataType("bt");
                v.setSelected(true);
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
}