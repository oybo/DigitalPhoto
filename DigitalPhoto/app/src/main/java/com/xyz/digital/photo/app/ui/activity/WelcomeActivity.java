package com.xyz.digital.photo.app.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.xyz.digital.photo.app.R;

/**
 * Created by O on 2017/3/18.
 */

public class WelcomeActivity extends Activity {

    private static Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goMain();
            }
        }, 1000);
    }

    private void goMain() {
        startActivity(new Intent(this, ConnectWifiActivity.class));
        finish();
    }

}
