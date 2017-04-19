package com.xyz.digital.photo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.BaseActivity;

/**
 * Created by Administrator on 2017/4/4.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mLogoImage;
    private CheckBox mCheckBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        initTopBarOnlyTitle("登录");

        mLogoImage = (ImageView) findViewById(R.id.login_logo_icon_image);
        mCheckBox = (CheckBox) findViewById(R.id.login_is_save_pwd);

        findViewById(R.id.login_login_bt).setOnClickListener(this);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
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
            case R.id.login_login_bt:
                startActivity(new Intent(LoginActivity.this, DeviceDetailActivity.class));
                finish();
                break;
        }
    }
}
