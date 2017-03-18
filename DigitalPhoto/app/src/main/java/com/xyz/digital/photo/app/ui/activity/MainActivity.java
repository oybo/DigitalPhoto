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
import com.xyz.digital.photo.app.ui.fragment.LocalFileFragment;
import com.xyz.digital.photo.app.ui.fragment.PhotoFileFragment;
import com.xyz.digital.photo.app.ui.fragment.PhotoSetFragment;
import com.xyz.digital.photo.app.ui.fragment.RemoteControlFragment;
import butterknife.Bind;

public class MainActivity extends BaseActivity {

    private static final String TAG_INDEX = "index";

    @Bind(R.id.fragment_container) RelativeLayout fragmentContainer;
    @Bind(R.id.main_local_file_bt) Button mainLocalFileBt;
    @Bind(R.id.main_digital_file_bt) Button mainDigitalFileBt;
    @Bind(R.id.main_digital_set_bt) Button mainDigitalSetBt;
    @Bind(R.id.main_remote_control_bt) Button mainRemoteControlBt;

    private int index = -1, currentTabIndex = -1;
    private Button[] mTabs;
    private Fragment[] fragments;
    private LocalFileFragment mLocalFileFragment;
    private PhotoFileFragment mPhotoFileFragment;
    private PhotoSetFragment mPhotoSetFragment;
    private RemoteControlFragment mRemoteControlFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initFragment(savedInstanceState);
    }

    private void initView() {
        mTabs = new Button[] { mainLocalFileBt, mainDigitalFileBt, mainDigitalSetBt, mainRemoteControlBt };
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            FragmentManager fm = getSupportFragmentManager();
            mLocalFileFragment = (LocalFileFragment) fm.findFragmentByTag("LocalFileFragment");
            mPhotoFileFragment = (PhotoFileFragment) fm.findFragmentByTag("PhotoFileFragment");
            mPhotoSetFragment = (PhotoSetFragment) fm.findFragmentByTag("PhotoSetFragment");
            mRemoteControlFragment = (RemoteControlFragment) fm.findFragmentByTag("RemoteControlFragment");

            index = savedInstanceState.getInt(TAG_INDEX);
        }
        if (mLocalFileFragment == null) {
            mLocalFileFragment = new LocalFileFragment();
        }
        if (mPhotoFileFragment == null) {
            mPhotoFileFragment = new PhotoFileFragment();
        }
        if (mPhotoSetFragment == null) {
            mPhotoSetFragment = new PhotoSetFragment();
        }
        if (mRemoteControlFragment == null) {
            mRemoteControlFragment = new RemoteControlFragment();
        }

        fragments = new Fragment[]{mLocalFileFragment, mPhotoFileFragment, mPhotoSetFragment, mRemoteControlFragment};

        if (index == -1) {
            index = 0;
        }
        currentTab();
    }

    public void onTabClicked(View view) {
        mTabs = new Button[] { mainLocalFileBt, mainDigitalFileBt, mainDigitalSetBt, mainRemoteControlBt };
        if (view == mainLocalFileBt) {
            index = 0;
        } else if (view== mainDigitalFileBt) {
            index = 1;
        } else if (view == mainDigitalSetBt) {
            index = 2;
        } else if (view == mainRemoteControlBt) {
            index = 3;
        }
        currentTab();
    }

    /**
     * 初始化Fragment切换
     */
    private void currentTab() {
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            if (currentTabIndex >= 0) {
                trx.hide(fragments[currentTabIndex]);
            }
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        if (currentTabIndex >= 0) {
            mTabs[currentTabIndex].setSelected(false);
        }
        // 把当前tab设为选中状态
        mTabs[index].setSelected(true);
        currentTabIndex = index;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
        // 存储下标
        outState.putInt(TAG_INDEX, index);
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
}
