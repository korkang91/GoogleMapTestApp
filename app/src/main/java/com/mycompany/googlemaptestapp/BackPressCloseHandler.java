package com.mycompany.googlemaptestapp;

/**
 * Created by manager on 2016. 12. 1..
 */

import android.app.Activity;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class BackPressCloseHandler {

    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;
    private InterstitialAd interstitialAd;
    String TAG = "kbc";

    public BackPressCloseHandler(Activity context) {
        this.activity = context;
        Start();
    }
    void Start(){
        interstitialAd = new InterstitialAd(this.activity);
        interstitialAd.setAdUnitId("ca-app-pub-8149923844085303/2365120078");
        //Create an empty ad request.
        AdRequest request = new AdRequest.Builder().build();
        //Load the Ads with the request.
        interstitialAd.loadAd(request);
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            toast.cancel();

            if(interstitialAd != null){
                interstitialAd.show();
            }
            activity.finish();
        }
    }

    public void showGuide() {
        toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
