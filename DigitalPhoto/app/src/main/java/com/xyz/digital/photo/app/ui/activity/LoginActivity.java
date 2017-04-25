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
import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.LoadingView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;

/**
 * Created by Administrator on 2017/4/4.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.view_loading)
    LoadingView mLoadingView;

    private ImageView mLogoImage;
    private CheckBox mCheckBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        initData();
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

    private void initData() {
        EventBus.getDefault().register(this);
        if(DeviceManager.getInstance().isConnect()) {
            // 如果已经连接成功，则自动到详情页面
            if(DeviceManager.getInstance().getRemoteDeviceFiles().size() == 0) {
                DeviceManager.getInstance().connect();
            }
            goMain();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBase eventBase) {
        String action = eventBase.getAction();
        if (action.equals(Constants.SEND_CONNECT_STATE)) {
            boolean success = (boolean) eventBase.getData();
            mLoadingView.hide();
            if (success) {
                ToastUtil.showToast(LoginActivity.this, "连接成功");
                goMain();
            } else {
                ToastUtil.showToast(LoginActivity.this, "连接失败");
            }
        }
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
                DeviceManager.getInstance().connect();
                break;
        }
    }

    private void goMain() {
        startActivity(new Intent(LoginActivity.this, DeviceDetailActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
