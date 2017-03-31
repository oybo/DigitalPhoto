package com.xyz.digital.photo.app.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.xyz.digital.photo.app.R;

/**
 * Created by O on 2017/3/18.
 */

public class WelcomeActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.welcome_begin_employ_bt).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
