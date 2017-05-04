package com.xyz.digital.photo.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.view.SelectDialog;
import com.xyz.digital.photo.app.view.SwitchButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/18.
 * 相框设置
 */

public class SetFragment extends BaseFragment implements View.OnClickListener {

    public static final String mImageShowScale_key = "mImageShowScale_key";
    public static final String mPlayTime_key = "mPlayTime_key";
    public static final String mPlayOrder_key = "mPlayOrder_key";
    public static final String mVideoShowScale_key = "mVideoShowScale_key";
    public static final String mVideoPlayModel_key = "mVideoPlayModel_key";
    public static final String mAudioPlayModel_key = "mAudioPlayModel_key";
    public static final String mStartPlayModel_key = "mStartPlayModel_key";
    private static final String[] mImageShowScale = new String[] { "全屏", "原始尺寸", "等比例缩放"};
    private static final String[] mPlayTime = new String[] { "3秒", "5秒", "15秒", "30秒", "1分钟", "5分钟", "15分钟", "30分钟", "1小时"};
    private static final String[] mPlayOrder = new String[] { "顺序", "随机"};
    private static final String[] mVideoShowScale = new String[] { "全屏", "原始尺寸", "等比例"};
    private static final String[] mVideoPlayModel = new String[] { "顺序", "单个重复", "全部重复", "随机", "随机+重复"};
    private static final String[] mAudioPlayModel = new String[] { "顺序", "单个重复", "全部重复", "随机", "随机+重复"};
    private static final String[] mStartPlayModel = new String[] { "关", "图片", "音乐", "视频", "图片+音乐", "日历"};

    @Bind(R.id.set_image_show_ratio_txt) TextView mImageShowRatioTxt;
    @Bind(R.id.set_image_play_time_txt) TextView mImagePlayTimeTxt;
    @Bind(R.id.set_image_play_order_txt) TextView mImagePlayOrderTxt;
    @Bind(R.id.set_video_show_scale_txt) TextView mVideoShowScaleTxt;
    @Bind(R.id.set_video_play_model_txt) TextView mVideoPlayModelTxt;
    @Bind(R.id.set_audio_play_model_txt) TextView mAudioPlayModelTxt;
    @Bind(R.id.set_start_play_model_txt) TextView mStartPlayModelTxt;
    @Bind(R.id.set_breakpoint_play_sb) SwitchButton mBreakpointPlayBt;
    @Bind(R.id.set_subtitle_sb) SwitchButton mSubtitleBt;
    @Bind(R.id.set_show_channel_sb) SwitchButton mShowChannelBt;

    private SelectDialog mSelectDialog;
    private List<String> mItemSelects = new ArrayList<>();
    private int mItemType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_set, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initData();
    }

    private void initView() {
        getView().findViewById(R.id.set_image_show_ratio_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_image_play_time_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_image_play_order_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_video_show_scale_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_video_play_model_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_audio_play_model_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_start_play_model_layout).setOnClickListener(this);
    }

    private void initData() {
        setImageShowRatioTxt();
        setImagePlayTimeTxt();
        setImagePlayOrderTxt();
        setVideoShowScaleTxt();
        setVideoPlayModelTxt();
        setAudioPlayModelTxt();
        setStartPlayModelTxt();

        // 读取配置文件
        String value = DeviceManager.getInstance().getpropertiesValue("photo_enter_mode");
        String sds = "";
    }

    private void setImageShowRatioTxt() {
        int position = PreferenceUtils.getInstance().getInt(mImageShowScale_key, 0);
        mImageShowRatioTxt.setText(mImageShowScale[position]);
    }

    private void setImagePlayTimeTxt() {
        int position = PreferenceUtils.getInstance().getInt(mPlayTime_key, 0);
        mImagePlayTimeTxt.setText(mPlayTime[position]);
    }

    private void setImagePlayOrderTxt() {
        int position = PreferenceUtils.getInstance().getInt(mPlayOrder_key, 0);
        mImagePlayOrderTxt.setText(mPlayOrder[position]);
    }

    private void setVideoShowScaleTxt() {
        int position = PreferenceUtils.getInstance().getInt(mVideoShowScale_key, 0);
        mVideoShowScaleTxt.setText(mVideoShowScale[position]);
    }

    private void setVideoPlayModelTxt() {
        int position = PreferenceUtils.getInstance().getInt(mVideoPlayModel_key, 0);
        mVideoPlayModelTxt.setText(mVideoPlayModel[position]);
    }

    private void setAudioPlayModelTxt() {
        int position = PreferenceUtils.getInstance().getInt(mAudioPlayModel_key, 0);
        mAudioPlayModelTxt.setText(mAudioPlayModel[position]);
    }

    private void setStartPlayModelTxt() {
        int position = PreferenceUtils.getInstance().getInt(mStartPlayModel_key, 0);
        mStartPlayModelTxt.setText(mStartPlayModel[position]);
    }

    @Override
    public void onClick(View v) {
        // 读取配置文件
        String value = DeviceManager.getInstance().getpropertiesValue("photo_enter_mode");
        String sds = "";
        mItemSelects.clear();
        switch (v.getId()) {
            case R.id.set_image_show_ratio_layout:
                // 图片显示比例
                mItemType = 1;
                for (String str : mImageShowScale) {
                    mItemSelects.add(str);
                }
                break;
            case R.id.set_image_play_time_layout:
                // 幻灯片放映时间
                mItemType = 2;
                for (String str : mPlayTime) {
                    mItemSelects.add(str);
                }
                break;
            case R.id.set_image_play_order_layout:
                // 幻灯片播放顺序
                mItemType = 3;
                for (String str : mPlayOrder) {
                    mItemSelects.add(str);
                }
                break;
            case R.id.set_video_show_scale_layout:
                // 视频显示比例
                mItemType = 4;
                for (String str : mVideoShowScale) {
                    mItemSelects.add(str);
                }
                break;
            case R.id.set_video_play_model_layout:
                // 视频播放模式
                mItemType = 5;
                for (String str : mVideoPlayModel) {
                    mItemSelects.add(str);
                }
                break;
            case R.id.set_audio_play_model_layout:
                // 音乐播放模式
                mItemType = 6;
                for (String str : mAudioPlayModel) {
                    mItemSelects.add(str);
                }
                break;
            case R.id.set_start_play_model_layout:
                // 开机播放模式
                mItemType = 7;
                for (String str : mStartPlayModel) {
                    mItemSelects.add(str);
                }
                break;
        }
        showSelectDialog(mItemType, mItemSelects);
    }

    private void showSelectDialog(final int type, final List<String> itemSelects) {
        mSelectDialog = new SelectDialog(getActivity());
        mSelectDialog.show(type, itemSelects, new SelectDialog.OnSelectListener() {
            @Override
            public void select(int position) {
                switch (type) {
                    case 1:
                        // 图片显示比例
                        PreferenceUtils.getInstance().putInt(mImageShowScale_key, position);
                        break;
                    case 2:
                        // 幻灯片放映时间
                        PreferenceUtils.getInstance().putInt(mPlayTime_key, position);
                        break;
                    case 3:
                        // 幻灯片播放顺序
                        PreferenceUtils.getInstance().putInt(mPlayOrder_key, position);
                        break;
                    case 4:
                        // 视频显示比例
                        PreferenceUtils.getInstance().putInt(mVideoShowScale_key, position);
                        break;
                    case 5:
                        // 视频播放模式
                        PreferenceUtils.getInstance().putInt(mVideoPlayModel_key, position);
                        break;
                    case 6:
                        // 音乐播放模式
                        PreferenceUtils.getInstance().putInt(mAudioPlayModel_key, position);
                        break;
                    case 7:
                        // 开机播放模式
                        PreferenceUtils.getInstance().putInt(mStartPlayModel_key, position);
                        break;
                }
                initData();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
