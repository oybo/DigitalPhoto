package com.xyz.digital.photo.app.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.ui.fragment.DeviceFragment;
import com.xyz.digital.photo.app.ui.fragment.PhotoFragment;
import com.xyz.digital.photo.app.ui.fragment.SetFragment;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private static final String TAB_INDEX = "tab_index";
    private static final String FRAGMENT_INDEX = "fragment_index";

    @Bind(R.id.fragment_container) RelativeLayout fragmentContainer;
    @Bind(R.id.main_device_bt) Button mainDeviceBt;
    @Bind(R.id.main_photo_bt) Button mainPhotoBt;
    @Bind(R.id.main_scan_bt) Button mainScanBt;
    @Bind(R.id.main_telecontrol_bt) Button mainTelecontrolBt;
    @Bind(R.id.main_set_bt) Button mainSetBt;

    private int oldTabIndex = -1, oldFragmentIndex = -1;
    private Button[] mTabs;
    private Fragment[] fragments;
    /**    设备      */
    private DeviceFragment mDeviceFragment;
    /**    相册      */
    private PhotoFragment mPhotoFragment;
    /**    设置      */
    private SetFragment mSetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
        initFragment(savedInstanceState);
    }

    private void initView() {
        mTabs = new Button[]{ mainDeviceBt, mainPhotoBt, mainScanBt, mainTelecontrolBt, mainSetBt };
    }

    private void initFragment(Bundle savedInstanceState) {
        int tab_index = 0;
        int fgrament_index = 0;
        if (savedInstanceState != null) {
            FragmentManager fm = getSupportFragmentManager();
            mDeviceFragment = (DeviceFragment) fm.findFragmentByTag(DeviceFragment.class.getName());
            mPhotoFragment = (PhotoFragment) fm.findFragmentByTag(PhotoFragment.class.getName());
            mSetFragment = (SetFragment) fm.findFragmentByTag(SetFragment.class.getName());

            tab_index = savedInstanceState.getInt(TAB_INDEX, 0);
            fgrament_index = savedInstanceState.getInt(FRAGMENT_INDEX, 0);
        }
        if (mDeviceFragment == null) {
            mDeviceFragment = new DeviceFragment();
        }
        if (mPhotoFragment == null) {
            mPhotoFragment = new PhotoFragment();
        }
        if (mSetFragment == null) {
            mSetFragment = new SetFragment();
        }

        fragments = new Fragment[]{ mDeviceFragment, mPhotoFragment, mSetFragment };

        currentTab(tab_index);
        currentFragment(fgrament_index);
    }

    public void onTabClicked(View view) {
        if (view == mainDeviceBt) {
            // 设备
            currentTab(0);
            currentFragment(0);
        } else if (view == mainPhotoBt) {
            // 相册
            currentTab(1);
            currentFragment(1);
        } else if (view == mainScanBt) {
            // 扫描
            currentTab(2);
        } else if (view == mainTelecontrolBt) {
            // 遥控
            currentTab(3);
        } else if (view == mainSetBt) {
            // 设置
            currentTab(4);
            currentFragment(2);
        }
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
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
