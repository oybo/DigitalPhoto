package com.xyz.digital.photo.app.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuItem;
import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuWidget;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.MyNestedScrollView;
import com.xyz.digital.photo.app.view.VerticalSeekBar;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/4.
 */

public class RemoteControlFragment extends BaseFragment implements View.OnClickListener {

    private RadialMenuWidget mMenuWidget;
    private RadialMenuItem mConfirmMenuItem, mUpMenuItem, mDownMenuItem, mLeftMenuItem, mRightMenuItem;

    private MyNestedScrollView mScrollView;
    private VerticalSeekBar mVoiceSeekBar, mBrightnessSeekBar;

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
        mScrollView = (MyNestedScrollView) getView().findViewById(R.id.fragment_control_scrollview);
        mVoiceSeekBar = (VerticalSeekBar) getView().findViewById(R.id.book_read_menu_voice_seekbar);
        mVoiceSeekBar.setMax(100);
        mVoiceSeekBar.setProgress(50);
        mBrightnessSeekBar = (VerticalSeekBar) getView().findViewById(R.id.book_read_menu_brightness_seekbar);
        mScrollView.addInterceptTouchView(mVoiceSeekBar);
        mScrollView.addInterceptTouchView(mBrightnessSeekBar);

        RelativeLayout layout = (RelativeLayout) getView().findViewById(R.id.book_read_menu_menu_widget_layout);
        mMenuWidget = new RadialMenuWidget(getActivity());
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(mMenuWidget, rlp);
        // 确定
        mConfirmMenuItem = new RadialMenuItem(getString(android.R.string.ok), getString(android.R.string.ok));
        mConfirmMenuItem.setOnMenuItemPressed(new MyMenuItemClickListener(0));
        // 中心按钮
        mMenuWidget.setCenterCircle(mConfirmMenuItem);
        // 添加上下左右菜单
        mUpMenuItem = createRadialMenuItem(1, R.drawable.control_remote_control_up);
        mDownMenuItem = createRadialMenuItem(2, R.drawable.control_remote_control_down);
        mLeftMenuItem = createRadialMenuItem(3, R.drawable.control_remote_control_left);
        mRightMenuItem = createRadialMenuItem(4, R.drawable.control_remote_control_right);

        mMenuWidget.setAnimationSpeed(0L);
        mMenuWidget.setSourceLocation(200, 200);
        mMenuWidget.setIconSize(15, 30);
        mMenuWidget.setTextSize(13);
        mMenuWidget.setOutlineColor(Color.BLACK, 225);
        mMenuWidget.setInnerRingColor(0xAA66CC, 180);
        mMenuWidget.setOuterRingColor(0x0099CC, 180);
        // 环形按钮
        mMenuWidget.addMenuEntry(new ArrayList<RadialMenuItem>() {
            {
                add(mUpMenuItem);
                add(mRightMenuItem);
                add(mDownMenuItem);
                add(mLeftMenuItem);
            }
        });

        // 电源
        getView().findViewById(R.id.fragment_control_tab_power_layout).setOnClickListener(this);
        // 设置
        getView().findViewById(R.id.fragment_control_tab_set_layou).setOnClickListener(this);
        // 背景音乐
        getView().findViewById(R.id.fragment_control_tab_music_layout).setOnClickListener(this);
        // 静音
        getView().findViewById(R.id.fragment_control_tab_mute_layout).setOnClickListener(this);
        // 图片
        getView().findViewById(R.id.fragment_control_menu_image_layout).setOnClickListener(this);
        // 音乐
        getView().findViewById(R.id.fragment_control_menu_music_layout).setOnClickListener(this);
        // 视频
        getView().findViewById(R.id.fragment_control_menu_video_layout).setOnClickListener(this);
        // 日历
        getView().findViewById(R.id.fragment_control_menu_calendar_layout).setOnClickListener(this);
        // 显示模式
        getView().findViewById(R.id.fragment_control_menu_show_model_layout).setOnClickListener(this);
        // 返回
        getView().findViewById(R.id.fragment_control_back_layout).setOnClickListener(this);
        // 主页
        getView().findViewById(R.id.fragment_control_main_layout).setOnClickListener(this);
        // 放大
        getView().findViewById(R.id.fragment_control_magnify_bt).setOnClickListener(this);
        // 缩小
        getView().findViewById(R.id.fragment_control_shrink_bt).setOnClickListener(this);
        // 上一首
        getView().findViewById(R.id.fragment_control_previou_bt).setOnClickListener(this);
        // 下一首
        getView().findViewById(R.id.fragment_control_next_bt).setOnClickListener(this);
        // 播放暂停
        getView().findViewById(R.id.fragment_control_play_bt).setOnClickListener(this);
    }

    private RadialMenuItem createRadialMenuItem(int type, int resouceId) {
        RadialMenuItem menuItem = new RadialMenuItem(null, " ");
        menuItem.setDisplayIcon(resouceId);
        menuItem.setOnMenuItemPressed(new MyMenuItemClickListener(type));
        return menuItem;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_control_tab_power_layout:
                // 电源

                break;
            case R.id.fragment_control_tab_set_layou:
                // 设置

                break;
            case R.id.fragment_control_tab_music_layout:
                // 背景音乐

                break;
            case R.id.fragment_control_tab_mute_layout:
                // 静音

                break;
            case R.id.fragment_control_menu_image_layout:
                // 图片

                break;
            case R.id.fragment_control_menu_music_layout:
                // 音乐

                break;
            case R.id.fragment_control_menu_video_layout:
                // 视频

                break;
            case R.id.fragment_control_menu_calendar_layout:
                // 日历

                break;
            case R.id.fragment_control_menu_show_model_layout:
                // 显示模式

                break;
            case R.id.fragment_control_back_layout:
                // 返回

                break;
            case R.id.fragment_control_main_layout:
                // 主页

                break;
            case R.id.fragment_control_magnify_bt:
                // 放大

                break;
            case R.id.fragment_control_shrink_bt:
                // 缩小

                break;
            case R.id.fragment_control_previou_bt:
                // 上一首

                break;
            case R.id.fragment_control_next_bt:
                // 下一首

                break;
            case R.id.fragment_control_play_bt:
                // 播放暂停

                break;
        }
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
