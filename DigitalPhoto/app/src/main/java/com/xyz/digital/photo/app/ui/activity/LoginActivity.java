package com.xyz.digital.photo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.actions.actcommunication.AcEventListener;
import com.actions.actcommunication.ActCommunication;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.LoadingView;

import butterknife.Bind;

/**
 * Created by Administrator on 2017/4/4.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.view_loading) LoadingView mLoadingView;

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

        // 连接监听
        ActCommunication.getInstance().setEventListener(mAcEventListener);

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
                mLoadingView.show();
                ActCommunication.getInstance().connect(Constants.HOST_IP);
                goMain();
                break;
        }
    }

    private void goMain() {
        startActivity(new Intent(LoginActivity.this, DeviceDetailActivity.class));
        finish();
    }

    private AcEventListener mAcEventListener = new AcEventListener() {

        @Override
        public void onDeviceConnected() {
            mLoadingView.hide();
            ToastUtil.showToast(LoginActivity.this, "连接成功");
            goMain();
        }

        @Override
        public void onDeviceDisconnect() {
            mLoadingView.hide();
            ToastUtil.showToast(LoginActivity.this, "连接失败");
            goMain();
        }

        @Override
        public void onRecvVolume(int volume) {

        }

        @Override
        public void onRecvTotalTime(int timeMs) {

        }

        @Override
        public void onRecvCurrentTime(int timeMs) {

        }

        @Override
        public void onRecvPlayerStatus(int status) {

        }

        @Override
        public void onRecvPlaySequence(int seq) {

        }

        @Override
        public void onRecvThumbnail(String url, byte[] data) {

        }
    };

}
