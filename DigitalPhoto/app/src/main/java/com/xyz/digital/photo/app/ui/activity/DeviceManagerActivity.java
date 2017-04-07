package com.xyz.digital.photo.app.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.BaseActivity;

/**
 * Created by O on 2017/4/5.
 */

public class DeviceManagerActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);

        initView();
        initData();
    }

    private void initView() {

    }

    private void initData() {

    }

    @Override
    protected int getToolBarMenuView() {
        return 0;
    }

    @Override
    protected void onItemMenuSelected(MenuItem item) {

    }
}
