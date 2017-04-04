package com.xyz.digital.photo.app.ui.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.ChangeLayout;

/**
 * Created by Administrator on 2017/4/4.
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mLogoImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        initTopBarOnlyTitle("登录");

        mLogoImage = (ImageView) findViewById(R.id.login_logo_icon_image);

        findViewById(R.id.login_forget_password_bt).setOnClickListener(this);

        ChangeLayout changeLayout = (ChangeLayout) findViewById(R.id.activity_login_changeview);
        changeLayout.setOnSizeChangedListener(new ChangeLayout.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                if(oldh != 0) {
                    if(h < oldh) {
                        // 关闭软键盘, mLogoImage大小还原
                        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mLogoImage, "scaleX", 1f, 0.6f);
                        ObjectAnimator anim2 = ObjectAnimator.ofFloat(mLogoImage, "scaleY", 1f, 0.6f);
                        AnimatorSet animSet = new AnimatorSet();
                        animSet.play(anim1).with(anim2);
                        animSet.start();
                    } else if(h > oldh) {
                        // 弹出软键盘, mLogoImage大小缩小一倍
                        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mLogoImage, "scaleX", 0.6f, 1f);
                        ObjectAnimator anim2 = ObjectAnimator.ofFloat(mLogoImage, "scaleY", 0.6f, 1f);
                        AnimatorSet animSet = new AnimatorSet();
                        animSet.play(anim1).with(anim2);
                        animSet.start();
                    }
                }
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

                break;
            case R.id.login_forget_password_bt:
                ToastUtil.showToast(LoginActivity.this, "忘记密码");
                break;
        }
    }
}
