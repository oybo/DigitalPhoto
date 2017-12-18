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

public class ScanActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        initView();
    }

    private void initView() {
        String title = getResources().getString(R.string.no_connect_device_txt);
        initTopBarOnlyTitle(title);

        findViewById(R.id.login_scanning_device_bt).setOnClickListener(this);
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
                startActivity(new Intent(ScanActivity.this, MainActivity.class));
                finish();
                break;
        }
    }
}
