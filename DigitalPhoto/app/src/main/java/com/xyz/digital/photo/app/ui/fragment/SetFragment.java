package com.xyz.digital.photo.app.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.PubUtils;
import com.xyz.digital.photo.app.view.AppInfoDialog;
import com.xyz.digital.photo.app.view.LoadingView;
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

    public static final String mImageShowScale_key = "photo_display_ratio";
    public static final String mPlayTime_key = "photo_slide_interval";
    public static final String mPlayOrder_key = "photo_play_mode";
    public static final String mVideoShowScale_key = "video_displaymode";
    public static final String mVideoPlayModel_key = "video_playmode";
    public static final String mAudioPlayModel_key = "music_play_mode";
    public static final String mStartPlayModel_key = "sys_startup_play";
    public static final String mSelectLanguage_key = "select_language_key";

    public static final String mBreakpointPlay_key = "video_resume_enable";
    public static final String mSubtitle_key = "video_subtitle";
    public static final String mShowSpectrum_key = "music_show_spectrum";

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
    @Bind(R.id.view_loading) LoadingView mLoadingView;

    @Bind(R.id.set_version_txt) TextView mVersionTxt;
    @Bind(R.id.set_language_txt) TextView mLanguageTxt;

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

        setSwitchListener(mBreakpointPlayBt, mBreakpointPlay_key);
        setSwitchListener(mSubtitleBt, mSubtitle_key);
        setSwitchListener(mShowChannelBt, mShowSpectrum_key);

        getView().findViewById(R.id.set_language_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_info_layout).setOnClickListener(this);
    }

    private void setSwitchListener(SwitchButton switchButton, final String key) {
        switchButton.setOnSwitchListener(new SwitchButton.OnSwitchListener() {
            @Override
            public void OnCheckListenr(boolean isCheck) {
                DeviceManager.getInstance().setpropertiesValue(key, !isCheck ? "0" : "1");
            }
        });
    }

    private void initData() {
        initTxt();
        initConfig();

        mVersionTxt.setText(PubUtils.getSoftVersion(getActivity()));
        mLanguageTxt.setText(mSelectLanguage[PreferenceUtils.getInstance().getInt(mSelectLanguage_key, 0)]);
    }

    private void initConfig() {
        // 图片显示比例
        initProperties(mImageShowScale_key, mImageShowScale_key, mImageShowRatioTxt, mImageShowScale);
        // 幻灯片放映时间
        initProperties(mPlayTime_key, mPlayTime_key, mImagePlayTimeTxt, mPlayTime);
        // 幻灯片播放顺序
        initProperties(mPlayOrder_key, mPlayOrder_key, mImagePlayOrderTxt, mPlayOrder);
        // 视频显示比例
        initProperties(mVideoShowScale_key, mVideoShowScale_key, mVideoShowScaleTxt, mVideoShowScale);
        // 视频播放模式
        initProperties(mVideoPlayModel_key, mVideoPlayModel_key, mVideoPlayModelTxt, mVideoPlayModel);
        // 音乐播放模式
        initProperties(mAudioPlayModel_key, mAudioPlayModel_key, mAudioPlayModelTxt, mAudioPlayModel);
        // 开机播放模式
        initProperties(mStartPlayModel_key, mStartPlayModel_key, mStartPlayModelTxt, mStartPlayModel);

        // 断点播放功能-开关
        initSwitch(mBreakpointPlay_key, mBreakpointPlayBt);
        // 字幕-开关
        initSwitch(mSubtitle_key, mSubtitleBt);
        // 显示频谱-开关
        initSwitch(mShowSpectrum_key, mShowChannelBt);
    }

    private void initSwitch(String key, SwitchButton switchButton) {
        try {
            String p = DeviceManager.getInstance().getpropertiesValue(key);
            switchButton.setCheck(Integer.parseInt(p) == 0 ? false : true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化配置文件
    private void initProperties(String p_key, String sp_key, TextView txtView, String[] values) {
        try {
            String p = DeviceManager.getInstance().getpropertiesValue(p_key);
            PreferenceUtils.getInstance().putInt(sp_key, Integer.parseInt(p));

            txtView.setText(values[Integer.parseInt(p)]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
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
            case R.id.set_language_layout:
                // 选择语言
                selectLanguage();
                return;
            case R.id.set_info_layout:
                // 功能介绍
                new AppInfoDialog(getActivity()).show();
                return;
        }
        showSelectDialog(mItemType, mItemSelects);
    }

    private void selectLanguage() {
        SelectDialog selectLanguageDialog = new SelectDialog(getActivity());
        List<String> mItemSelects = new ArrayList<>();
        for(String str : mSelectLanguage) {
            mItemSelects.add(str);
        }
        selectLanguageDialog.show(8, mItemSelects, new SelectDialog.OnSelectListener() {
            @Override
            public void select(int position) {
                PreferenceUtils.getInstance().putInt(mSelectLanguage_key, position);
                mLanguageTxt.setText(mSelectLanguage[PreferenceUtils.getInstance().getInt(mSelectLanguage_key, 0)]);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mLoadingView.show();
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        getActivity().recreate();
                        mLoadingView.hide();
                    }
                }.execute();
            }
        });
    }

    private void showSelectDialog(final int type, final List<String> itemSelects) {
        mSelectDialog = new SelectDialog(getActivity());
        mSelectDialog.show(type, itemSelects, new SelectDialog.OnSelectListener() {
            @Override
            public void select(int position) {
                switch (type) {
                    case 1:
                        // 图片显示比例
                        DeviceManager.getInstance().setpropertiesValue(mImageShowScale_key, String.valueOf(position));
                        break;
                    case 2:
                        // 幻灯片放映时间
                        DeviceManager.getInstance().setpropertiesValue(mPlayTime_key, String.valueOf(position));
                        break;
                    case 3:
                        // 幻灯片播放顺序
                        DeviceManager.getInstance().setpropertiesValue(mPlayOrder_key, String.valueOf(position));
                        break;
                    case 4:
                        // 视频显示比例
                        DeviceManager.getInstance().setpropertiesValue(mVideoShowScale_key, String.valueOf(position));
                        break;
                    case 5:
                        // 视频播放模式
                        DeviceManager.getInstance().setpropertiesValue(mVideoPlayModel_key, String.valueOf(position));
                        break;
                    case 6:
                        // 音乐播放模式
                        DeviceManager.getInstance().setpropertiesValue(mAudioPlayModel_key, String.valueOf(position));
                        break;
                    case 7:
                        // 开机播放模式
                        DeviceManager.getInstance().setpropertiesValue(mStartPlayModel_key, String.valueOf(position));
                        break;
                }
                initConfig();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private static String[] mImageShowScale = new String[]{AppContext.getInstance().getSString(R.string.quanping_txt),
            AppContext.getInstance().getSString(R.string.yscc_txt), AppContext.getInstance().getSString(R.string.dblsf_txt)};
    private static String[] mPlayTime = new String[]{AppContext.getInstance().getSString(R.string.time_sm_txt),
            AppContext.getInstance().getSString(R.string.time_wm_txt), AppContext.getInstance().getSString(R.string.time_swm_txt),
            AppContext.getInstance().getSString(R.string.time_ssm_txt), AppContext.getInstance().getSString(R.string.time_yfz_txt),
            AppContext.getInstance().getSString(R.string.time_wfz_txt), AppContext.getInstance().getSString(R.string.time_swfz_txt),
            AppContext.getInstance().getSString(R.string.time_ssfz_txt), AppContext.getInstance().getSString(R.string.time_yxs_txt)};
    private static String[] mPlayOrder = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
            .getInstance().getSString(R.string.suiji_txt)};
    private static String[] mVideoShowScale = new String[]{AppContext.getInstance().getSString(R.string.quanping_txt), AppContext
            .getInstance().getSString(R.string.yscc_txt), AppContext.getInstance().getSString(R.string.dblsf_txt)};
    private static String[] mVideoPlayModel = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
            .getInstance().getSString(R.string.sangecf_txt),
            AppContext.getInstance().getSString(R.string.quanbucf_txt), AppContext.getInstance().getSString(R.string.suiji_txt),
            AppContext.getInstance().getSString(R.string.suijijchongf_txt)};
    private static String[] mAudioPlayModel = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
            .getInstance().getSString(R.string.sangecf_txt),
            AppContext.getInstance().getSString(R.string.quanbucf_txt), AppContext.getInstance().getSString(R.string.suiji_txt),
            AppContext.getInstance().getSString(R.string.suijijchongf_txt)};
    private static String[] mStartPlayModel = new String[]{AppContext.getInstance().getSString(R.string.close_txt), AppContext
            .getInstance().getSString(R.string.image_txt),
            AppContext.getInstance().getSString(R.string.music_txt), AppContext.getInstance().getSString(R.string.video_txt), AppContext
            .getInstance().getSString(R.string.image_music_txt),
            AppContext.getInstance().getSString(R.string.set_calendar_txt)};
    private static String[] mSelectLanguage = new String[] { "简体中文", "English"};

    private void initTxt() {
        mImageShowScale = new String[]{AppContext.getInstance().getSString(R.string.quanping_txt),
                AppContext.getInstance().getSString(R.string.yscc_txt), AppContext.getInstance().getSString(R.string.dblsf_txt)};
        mPlayTime = new String[]{AppContext.getInstance().getSString(R.string.time_sm_txt),
                AppContext.getInstance().getSString(R.string.time_wm_txt), AppContext.getInstance().getSString(R.string.time_swm_txt),
                AppContext.getInstance().getSString(R.string.time_ssm_txt), AppContext.getInstance().getSString(R.string.time_yfz_txt),
                AppContext.getInstance().getSString(R.string.time_wfz_txt), AppContext.getInstance().getSString(R.string.time_swfz_txt),
                AppContext.getInstance().getSString(R.string.time_ssfz_txt), AppContext.getInstance().getSString(R.string.time_yxs_txt)};
        mPlayOrder = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
                .getInstance().getSString(R.string.suiji_txt)};
        mVideoShowScale = new String[]{AppContext.getInstance().getSString(R.string.quanping_txt), AppContext
                .getInstance().getSString(R.string.yscc_txt), AppContext.getInstance().getSString(R.string.dblsf_txt)};
        mVideoPlayModel = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
                .getInstance().getSString(R.string.sangecf_txt),
                AppContext.getInstance().getSString(R.string.quanbucf_txt), AppContext.getInstance().getSString(R.string.suiji_txt),
                AppContext.getInstance().getSString(R.string.suijijchongf_txt)};
        mAudioPlayModel = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
                .getInstance().getSString(R.string.sangecf_txt),
                AppContext.getInstance().getSString(R.string.quanbucf_txt), AppContext.getInstance().getSString(R.string.suiji_txt),
                AppContext.getInstance().getSString(R.string.suijijchongf_txt)};
        mStartPlayModel = new String[]{AppContext.getInstance().getSString(R.string.close_txt), AppContext
                .getInstance().getSString(R.string.image_txt),
                AppContext.getInstance().getSString(R.string.music_txt), AppContext.getInstance().getSString(R.string.video_txt), AppContext
                .getInstance().getSString(R.string.image_music_txt),
                AppContext.getInstance().getSString(R.string.set_calendar_txt)};
        mSelectLanguage = new String[] { "简体中文", "English"};
    }

}
