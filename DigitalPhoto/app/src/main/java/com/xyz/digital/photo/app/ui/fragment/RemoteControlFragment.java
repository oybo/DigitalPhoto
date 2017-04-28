package com.xyz.digital.photo.app.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.actions.actcommunication.ActCommunication;
import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuItem;
import com.touchmenotapps.widget.radialmenu.menu.v1.RadialMenuWidget;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.util.Constants;
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
    private ImageView mPlayOrStopBt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_remote_control, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initData();
    }

    private void initView() {
        mScrollView = (MyNestedScrollView) getView().findViewById(R.id.fragment_control_scrollview);
        mVoiceSeekBar = (VerticalSeekBar) getView().findViewById(R.id.book_read_menu_voice_seekbar);
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
        mPlayOrStopBt = (ImageView) getView().findViewById(R.id.fragment_control_play_bt);
        mPlayOrStopBt.setOnClickListener(this);
        // 声音
        mVoiceSeekBar.setTag("volume");
        mVoiceSeekBar.setOnSeekBarChangeListener(mSeekListener);
        // 亮度
        mBrightnessSeekBar.setTag("brightness");
        mBrightnessSeekBar.setOnSeekBarChangeListener(mSeekListener);
    }

    private VerticalSeekBar.OnSeekBarChangeListener mSeekListener = new VerticalSeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
        }
        @Override
        public void onStartTrackingTouch(VerticalSeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(VerticalSeekBar seekBar) {
            String tag = (String) seekBar.getTag();
            if("volume".equals(tag)) {
                // 声音
                int progress = seekBar.getProgress();
                ActCommunication.getInstance().setVolume(progress);
            } else if("brightness".equals(tag)) {
                // 亮度
                int progress = seekBar.getProgress();

            }
        }
        @Override
        public void onrequestDisallowInterceptTouchEvent(boolean enable) {
        }
    };

    private void initData() {
        DeviceManager.getInstance().addOnCmdBackListener(new DeviceManager.OnCmdBackListener() {
            @Override
            public void onVolume(int value) {
                mVoiceSeekBar.setMax(Constants.MAX_VOLUME);
                mVoiceSeekBar.setProgress(value);
            }
        });
        ActCommunication.getInstance().requestVolume();
    }

    private RadialMenuItem createRadialMenuItem(int type, int resouceId) {
        RadialMenuItem menuItem = new RadialMenuItem(null, " ");
        menuItem.setDisplayIcon(resouceId);
        menuItem.setOnMenuItemPressed(new MyMenuItemClickListener(type));
        return menuItem;
    }

    private void sendCmd(String[] cmd) {
        ActCommunication.getInstance().sendMsg(cmd);
    }

    private boolean isPlay;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_control_tab_power_layout:
                // 电源
                ActCommunication.getInstance().powerOff();
                break;
            case R.id.fragment_control_tab_set_layou:
                // 设置
                sendCmd(new String[]{"cmd", "setting"});
                break;
            case R.id.fragment_control_tab_music_layout:
                // 背景音乐
                sendCmd(new String[]{"cmd", "photomusic"});
                break;
            case R.id.fragment_control_tab_mute_layout:
                // 静音
                sendCmd(new String[]{"cmd", "noVolume"});
                break;
            case R.id.fragment_control_menu_image_layout:
                // 图片
                sendCmd(new String[]{"cmd", "photo"});
                break;
            case R.id.fragment_control_menu_music_layout:
                // 音乐
                sendCmd(new String[]{"cmd", "music"});
                break;
            case R.id.fragment_control_menu_video_layout:
                // 视频
                sendCmd(new String[]{"cmd", "video"});
                break;
            case R.id.fragment_control_menu_calendar_layout:
                // 日历
                sendCmd(new String[]{"cmd", "calendar"});
                break;
            case R.id.fragment_control_menu_show_model_layout:
                // 显示模式
                sendCmd(new String[]{"cmd", "displayratio"});
                break;
            case R.id.fragment_control_back_layout:
                // 返回
                sendCmd(new String[]{"cmd", "return"});
                break;
            case R.id.fragment_control_main_layout:
                // 主页
                sendCmd(new String[]{"cmd", "home"});
                break;
            case R.id.fragment_control_magnify_bt:
                // 放大
                sendCmd(new String[]{"cmd", "zoomin"});
                break;
            case R.id.fragment_control_shrink_bt:
                // 缩小
                sendCmd(new String[]{"cmd", "zoomout"});
                break;
            case R.id.fragment_control_previou_bt:
                // 上一首
                sendCmd(new String[]{"cmd", "up"});
                break;
            case R.id.fragment_control_next_bt:
                // 下一首
                sendCmd(new String[]{"cmd", "down"});
                break;
            case R.id.fragment_control_play_bt:
                // 播放暂停
                isPlay = !isPlay;
                if(isPlay) {
                    // 播放
                    sendCmd(new String[]{"cmd", "stop"});
                    mPlayOrStopBt.setImageResource(R.drawable.control_stop_src_icon);
                } else {
                    // 暂停
                    sendCmd(new String[]{"cmd", "pause"});
                    mPlayOrStopBt.setImageResource(R.drawable.control_play_src_icon);
                }
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
                    // 确定
                    sendCmd(new String[]{"cmd", "ok"});
                    break;
                case 1:
                    // 上
                    sendCmd(new String[]{"cmd", "up"});
                    break;
                case 2:
                    // 下
                    sendCmd(new String[]{"cmd", "down"});
                    break;
                case 3:
                    // 左
                    sendCmd(new String[]{"cmd", "prev"});
                    break;
                case 4:
                    // 右
                    sendCmd(new String[]{"cmd", "next"});
                    break;
            }
        }
    }

}
