package com.mycompany.googlemaptestapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * Created by manager on 2016. 11. 28..
 */

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("log","kbc splash");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler hd = new Handler();
        hd.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();       // 3 초후 이미지를 닫아버림
            }
        }, 3000);

    }
}