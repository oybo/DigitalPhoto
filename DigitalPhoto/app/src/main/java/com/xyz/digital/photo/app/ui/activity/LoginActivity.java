package com.xyz.digital.photo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.BaseActivity;

/**
 * Created by O on 2017/3/31.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        initTopBarOnlyTitle("设备连接");

        findViewById(R.id.login_scanning_device_bt).setOnClickListener(this);
        findViewById(R.id.login_telnet_bt).setOnClickListener(this);
    }

    @Override
    protected int getToolBarMenuView() {
        return 0;
    }

    @Override
    protected void onItemMenuSelected(MenuItem item) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_scanning_device_bt:
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                break;
            case R.id.login_telnet_bt:

                break;
        }
    }
}
