package com.xyz.digital.photo.app.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuItem;
import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuWidget;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.util.ToastUtil;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/4.
 */

public class RemoteControlFragment extends BaseFragment {

    private RadialMenuWidget mMenuWidget;
    private RadialMenuItem mConfirmMenuItem, mUpMenuItem, mDownMenuItem, mLeftMenuItem, mRightMenuItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_remote_control, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
    }

    private void initView() {
        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.book_read_menu_layout);
        mMenuWidget = new RadialMenuWidget(getActivity());
        // 确定
        mConfirmMenuItem = new RadialMenuItem(getString(android.R.string.ok), "");
        mConfirmMenuItem.setOnMenuItemPressed(new MyMenuItemClickListener(0));
        // 添加上下左右菜单
        mUpMenuItem = createRadialMenuItem(1, R.drawable.login_pwd_icon);
        mDownMenuItem = createRadialMenuItem(2, R.drawable.login_pwd_icon);
        mLeftMenuItem = createRadialMenuItem(3, R.drawable.login_pwd_icon);
        mRightMenuItem = createRadialMenuItem(4, R.drawable.login_pwd_icon);

        mMenuWidget.setAnimationSpeed(0L);
        mMenuWidget.setSourceLocation(200, 200);
        mMenuWidget.setIconSize(15, 30);
        mMenuWidget.setTextSize(13);
        mMenuWidget.setOutlineColor(Color.BLACK, 225);
        mMenuWidget.setInnerRingColor(0xAA66CC, 180);
        mMenuWidget.setOuterRingColor(0x0099CC, 180);
        // 中心按钮
        mMenuWidget.setCenterCircle(mConfirmMenuItem);
        // 环形按钮
        mMenuWidget.addMenuEntry(new ArrayList<RadialMenuItem>() {
            {
                add(mUpMenuItem);
                add(mDownMenuItem);
                add(mLeftMenuItem);
                add(mRightMenuItem);
            }
        });
        mMenuWidget.show(frameLayout);
    }

    private RadialMenuItem createRadialMenuItem(int type, int resouceId) {
        RadialMenuItem menuItem = new RadialMenuItem("", "");
        menuItem.setDisplayIcon(resouceId);
        menuItem.setOnMenuItemPressed(new MyMenuItemClickListener(type));
        return menuItem;
    }

    private class MyMenuItemClickListener implements RadialMenuItem.RadialMenuItemClickListener {

        private int type;

        public MyMenuItemClickListener(int type) {
            this.type = type;
        }

        @Override
        public void execute() {
            switch (type) {
                case 0:
                    ToastUtil.showToast(getActivity(), "确定");
                    break;
                case 1:
                    ToastUtil.showToast(getActivity(), "上");
                    break;
                case 2:
                    ToastUtil.showToast(getActivity(), "下");
                    break;
                case 3:
                    ToastUtil.showToast(getActivity(), "左");
                    break;
                case 4:
                    ToastUtil.showToast(getActivity(), "右");
                    break;
            }
        }
    }

}
