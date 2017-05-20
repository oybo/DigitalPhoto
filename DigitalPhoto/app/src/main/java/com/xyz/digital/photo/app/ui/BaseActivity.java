package com.xyz.digital.photo.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.SysConfigHelper;
import com.xyz.digital.photo.app.util.SystemBarUtil;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.DialogTips;
import com.xyz.digital.photo.app.view.HeaderView;

import java.util.Locale;

import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/18.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSystemBarTint();

        //根据上次的语言设置，重新设置语言
        switchLanguage();
    }

    protected void switchLanguage() {
        //设置应用语言类型
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();

        int id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mSelectLanguage_key, 0);
        if (id == 1) {
            // 英文
            config.locale = Locale.ENGLISH;
        } else {
            // 中文
            config.locale = Locale.getDefault();
        }
        resources.updateConfiguration(config, dm);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        ButterKnife.bind(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        ButterKnife.bind(this);
    }

    public void initToolBar(Toolbar toolbar, boolean homeAsUpEnabled, int resTitle) {
        initToolBar(toolbar, homeAsUpEnabled, getString(resTitle));
    }

    /**
     * 初始化 Toolbar
     */
    public void initToolBar(Toolbar toolbar, boolean homeAsUpEnabled, String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(homeAsUpEnabled);
    }

    /**
     * 子类可以重写改变状态栏颜色
     */
    protected int setStatusBarColor() {
        return getColorPrimary();
    }

    /**
     * 获取主题色
     */
    public int getColorPrimary() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    /**
     * 子类可以重写决定是否使用透明状态栏
     */
    protected boolean translucentStatusBar() {
        return false;
    }

    /**
     * 设置状态栏颜色
     */
    protected void initSystemBarTint() {
        Window window = getWindow();
        if (translucentStatusBar()) {
            // 设置状态栏全透明
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            return;
        }
        // 沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0以上使用原生方法
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(setStatusBarColor());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4-5.0使用三方工具类，有些4.4的手机有问题，这里为演示方便，不使用沉浸式
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarUtil systemBarUtil = new SystemBarUtil(this);
            systemBarUtil.setStatusBarTintEnabled(true);
            systemBarUtil.setStatusBarTintColor(setStatusBarColor());
        }
    }

    protected void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));

                //底部导航栏
                //window.setNavigationBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showSimpleTipDialog(Context context, String message) {
        try {
            DialogTips dialogTips = new DialogTips(context);
            dialogTips.setMessage(message);
            dialogTips.setOkListenner(null);
            dialogTips.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showTipDialog(Context context, String message, DialogTips.onDialogOkListenner listenner) {
        showTipDialog(context, message, listenner, true);
    }

    protected void showTipDialog(Context context, String message, DialogTips.onDialogOkListenner listenner, boolean isTouchCancel) {

        try {
            DialogTips dialogTips = new DialogTips(context);
            dialogTips.setMessage(message);
            dialogTips.setOkListenner(listenner);
            if (!isTouchCancel) {
                dialogTips.setCancelable(false);
                dialogTips.setCanceledOnTouchOutside(false);
            }
            dialogTips.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showTipDialog(Context context, String message, DialogTips.onDialogOkListenner listenner,
                                 DialogTips.onDialogCancelListenner cancelListenner, boolean isTouchCancel) {
        try {
            DialogTips dialogTips = new DialogTips(context);
            dialogTips.setMessage(message);
            dialogTips.setCancelListenner(cancelListenner);
            dialogTips.setOkListenner(listenner);
            if (!isTouchCancel) {
                dialogTips.setCancelable(false);
                dialogTips.setCanceledOnTouchOutside(false);
            }
            dialogTips.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showToast(String msg) {
        ToastUtil.showToast(this, msg);
    }

    protected void initTopBarOnlyTitle(String title) {
        HeaderView headerView = (HeaderView) findViewById(R.id.actionbar_headerview);
        if (headerView != null) {
            headerView.setTitile(title);
            headerView.getToolbar().setTitle("");
            headerView.getToolbar().setNavigationIcon(R.drawable.finish_icon);
            setSupportActionBar(headerView.getToolbar());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 统一的返回按钮
            finish();
        } else {
            onItemMenuSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract int getToolBarMenuView();

    protected abstract void onItemMenuSelected(MenuItem item);
}
