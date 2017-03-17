package com.xyz.digital.photo.app.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.xyz.digital.photo.app.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_INDEX = "index";
    private int index = -1, currentTabIndex = -1;
    private Button[] mTabs;
    private Fragment[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragment(savedInstanceState);

    }

    private void initFragment(Bundle savedInstanceState) {

    }

    public void onTabClicked(View view) {
        if (view.getId() == R.id.main_local_file_bt) {
            index = 0;
        } else if (view.getId() == R.id.main_digital_file_bt) {
            index = 1;
        } else if (view.getId() == R.id.main_digital_set_bt) {
            index = 2;
        } else if (view.getId() == R.id.main_remote_control_bt) {
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

}
