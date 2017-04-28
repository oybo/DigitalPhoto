package com.xyz.digital.photo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.ui.fragment.DeviceFragment;
import com.xyz.digital.photo.app.ui.fragment.DevicePhotoFragment;
import com.xyz.digital.photo.app.ui.fragment.PhotoFragment;
import com.xyz.digital.photo.app.ui.fragment.RemoteControlFragment;
import com.xyz.digital.photo.app.ui.fragment.SetFragment;
import com.xyz.digital.photo.app.ui.fragment.WiFiDeviceFragment;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private static final String TAB_INDEX = "tab_index";
    private static final String FRAGMENT_INDEX = "fragment_index";

    @Bind(R.id.fragment_container) RelativeLayout fragmentContainer;
    @Bind(R.id.main_device_bt) TextView mainDeviceBt;
    @Bind(R.id.main_photo_bt) TextView mainPhotoBt;
    @Bind(R.id.main_telecontrol_bt) TextView mainTelecontrolBt;
    @Bind(R.id.main_set_bt) TextView mainSetBt;

    private int oldTabIndex = -1, oldFragmentIndex = -1;
    private TextView[] mTabs;
    private Fragment[] fragments;
    /**    设备 - wifi热点      */
    private WiFiDeviceFragment mWiFiDeviceFragment;
    /**    设备 - wifi直连     */
    private DeviceFragment mDeviceFragment;
    /**    设备相册管理      */
    private DevicePhotoFragment mDevicePhotoFragment;
    /**    相册      */
    private PhotoFragment mPhotoFragment;
    /**    遥控器      */
    private RemoteControlFragment mRemoteControlFragment;
    /**    设置      */
    private SetFragment mSetFragment;

    private boolean mLoginMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
        initFragment(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String type = intent.getStringExtra("type");
        if(Constants.MAIN_DEVICE_POHOTO_MANAGER.equals(type)) {
            mLoginMain = true;
            currentFragment(4);
        } else if(Constants.MAIN_DEVICE_LIST.equals(type)) {
            mLoginMain = false;
            currentFragment(0);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBase eventBase) {
        String action = eventBase.getAction();
        if(action.equals(Constants.REFRESH_STATUSBAR_COLOR)) {
            if(oldFragmentIndex == 2) {
                setWindowStatusBarColor(MainActivity.this, R.color.color_F2F2F2);
            } else {
                setWindowStatusBarColor(MainActivity.this, R.color.colorPrimaryDark);
            }
        }
    }

    private void initView() {
        mTabs = new TextView[]{ mainDeviceBt, mainPhotoBt, mainTelecontrolBt, mainSetBt };
    }

    private void initFragment(Bundle savedInstanceState) {
        int tab_index = 0;
        int fgrament_index = 0;
        if (savedInstanceState != null) {
            FragmentManager fm = getSupportFragmentManager();
            mWiFiDeviceFragment = (WiFiDeviceFragment) fm.findFragmentByTag(WiFiDeviceFragment.class.getName());
            mDeviceFragment = (DeviceFragment) fm.findFragmentByTag(DeviceFragment.class.getName());
            mDevicePhotoFragment = (DevicePhotoFragment) fm.findFragmentByTag(DevicePhotoFragment.class.getName());
            mPhotoFragment = (PhotoFragment) fm.findFragmentByTag(PhotoFragment.class.getName());
            mSetFragment = (SetFragment) fm.findFragmentByTag(SetFragment.class.getName());

            tab_index = savedInstanceState.getInt(TAB_INDEX, 0);
            fgrament_index = savedInstanceState.getInt(FRAGMENT_INDEX, 0);
        }
        if (mWiFiDeviceFragment == null) {
            mWiFiDeviceFragment = new WiFiDeviceFragment();
        }
        if (mDeviceFragment == null) {
            mDeviceFragment = new DeviceFragment();
        }
        if (mDevicePhotoFragment == null) {
            mDevicePhotoFragment = new DevicePhotoFragment();
        }
        if (mPhotoFragment == null) {
            mPhotoFragment = new PhotoFragment();
        }
        if (mRemoteControlFragment == null) {
            mRemoteControlFragment = new RemoteControlFragment();
        }
        if (mSetFragment == null) {
            mSetFragment = new SetFragment();
        }

        fragments = new Fragment[]{ mWiFiDeviceFragment, mPhotoFragment, mRemoteControlFragment, mSetFragment, mDevicePhotoFragment };

        currentTab(tab_index);
        currentFragment(fgrament_index);
    }

    public void onTabClicked(View view) {
        int tabIndex = 0;
        int fragmentIndex = 0;
        if (view == mainDeviceBt) {
            // 设备
            tabIndex = 0;
            fragmentIndex = 0;
            if(mLoginMain) {
                fragmentIndex = 4;
            }
        } else if (view == mainPhotoBt) {
            // 相册
            tabIndex = 1;
            fragmentIndex = 1;
        } else if (view == mainTelecontrolBt) {
            // 遥控
            tabIndex = 2;
            fragmentIndex = 2;
        } else if (view == mainSetBt) {
            // 设置
            tabIndex = 3;
            fragmentIndex = 3;
        }
        if(tabIndex > 0 && !DeviceManager.getInstance().isConnect()) {
            ToastUtil.showToast(MainActivity.this, "请您先连接设备");
            return;
        }
        currentTab(tabIndex);
        currentFragment(fragmentIndex);
    }

    /**
     * Tab按钮切换
     */
    private void currentTab(int index) {
        if (oldTabIndex >= 0) {
            mTabs[oldTabIndex].setSelected(false);
        }
        // 把当前tab设为选中状态
        mTabs[index].setSelected(true);
        oldTabIndex = index;
    }

    /**
     * Fragment切换
     */
    private void currentFragment(int index) {
        if (oldFragmentIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            if (oldFragmentIndex >= 0) {
                trx.hide(fragments[oldFragmentIndex]);
            }
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index], fragments[index].getClass().getName());
            }
            trx.show(fragments[index]).commit();
        }
        oldFragmentIndex = index;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
        // 存储下标
        outState.putInt(TAB_INDEX, oldTabIndex);
        outState.putInt(FRAGMENT_INDEX, oldFragmentIndex);
    }

    @Override
    protected boolean translucentStatusBar() {
        return false;
    }

    @Override
    protected int getToolBarMenuView() {
        return 0;
    }

    @Override
    protected void onItemMenuSelected(MenuItem item) {

    }

    @Override
    public void onBackPressed() {
        back();
    }

    private long mExitTime;

    private void back() {
        if(mPhotoFragment.isShowSelect()) {
            mPhotoFragment.closeSelect();
            return;
        }
        if(mPhotoFragment.isShowChild()) {
            mPhotoFragment.closeShowChild();
            return;
        }

        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            ToastUtil.showToast(this, "再按一次退出程序");
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
            System.exit(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
