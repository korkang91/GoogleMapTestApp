package com.mycompany.googlemaptestapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;


public class SplashActivity extends Activity {
    AlertDialog.Builder ab;
    int locationMode;   //GPS모드 확인 변수
    String TAG = "kbc";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        ab = new AlertDialog.Builder(this);
        ab.setTitle("위치 서비스 사용")
                .setMessage("이 앱에서 내 위치 정보를 사용하려면 단말기의 설정에서 '위치 서비스' 사용을 허용해주세요.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent busi_intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        busi_intent.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivity(busi_intent);
                    }
                })
                .setCancelable(false)
                .create();

        try {
            locationMode = Settings.Secure.getInt(SplashActivity.this.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (locationMode == 0) {
            ab.show();
        } else {
            int secondsDelayed = 1;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    startActivity(new Intent(SplashActivity.this, MapsActivity.class));
                    finish();
                }
            }, secondsDelayed * 1000);
        }


    }
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        startActivity(new Intent(SplashActivity.this, MapsActivity.class));
        finish();
    }

}