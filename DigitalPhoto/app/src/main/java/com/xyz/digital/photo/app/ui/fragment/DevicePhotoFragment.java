package com.xyz.digital.photo.app.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actions.actcommunication.ActCommunication;
import com.actions.actfilemanager.ActFileInfo;
import com.picker.TimePickerHelper;
import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.DeviceListMediaAdapter;
import com.xyz.digital.photo.app.adapter.DeviceMediaAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.DownloadInfo;
import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.bean.FileInfo;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.ui.activity.MainActivity;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.PubUtils;
import com.xyz.digital.photo.app.util.SysConfigHelper;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.ChooseModePopView;
import com.xyz.digital.photo.app.view.DialogTips;
import com.xyz.digital.photo.app.view.DividerItemDecoration;
import com.xyz.digital.photo.app.view.LoadingView;
import com.xyz.digital.photo.app.view.ProgressPieView;
import com.xyz.digital.photo.app.view.SelectDialog;
import com.xyz.digital.photo.app.view.SwitchButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.xyz.digital.photo.app.manager.DeviceManager.mRemoteCurrentPath;
import static com.xyz.digital.photo.app.util.SysConfigHelper.calendar_alarm_freq;
import static com.xyz.digital.photo.app.util.SysConfigHelper.calendar_alarm_time;
import static com.xyz.digital.photo.app.util.SysConfigHelper.calendar_date;
import static com.xyz.digital.photo.app.util.SysConfigHelper.calendar_time;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mAlarmFrequency;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mAudioPlayModel;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mAudioPlayModel_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mAutoPowerRequency;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mBg_Music_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mBreakpointPlay_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mImageShowScale;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mImageShowScale_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mNz_dskj_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mNz_dskj_off_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mNz_states_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mPlayOrder;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mPlayOrder_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mPlayTime;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mPlayTime_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mShowSpectrum_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mStartPlayModel;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mStartPlayModel_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mSubtitle_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mVideoPlayModel;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mVideoPlayModel_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mVideoShowScale;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mVideoShowScale_key;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mVolume;
import static com.xyz.digital.photo.app.util.SysConfigHelper.sys_auto_off_time;
import static com.xyz.digital.photo.app.util.SysConfigHelper.sys_auto_on_time;
import static com.xyz.digital.photo.app.util.SysConfigHelper.sys_auto_power_freq;
import static com.xyz.digital.photo.app.util.SysConfigHelper.sys_lang_codes;
import static com.xyz.digital.photo.app.util.SysConfigHelper.sys_lang_codes_all;
import static com.xyz.digital.photo.app.util.SysConfigHelper.sys_ver;
import static com.xyz.digital.photo.app.util.SysConfigHelper.sys_volume;

/**
 * Created by O on 2017/4/12.
 */

public class DevicePhotoFragment extends BaseFragment implements View.OnClickListener, BaseRecyclerAdapter.onInternalClickListener {

    @Bind(R.id.set_image_show_ratio_txt) TextView mImageShowRatioTxt;
    @Bind(R.id.set_image_play_time_txt) TextView mImagePlayTimeTxt;
    @Bind(R.id.set_image_play_order_txt) TextView mImagePlayOrderTxt;
    @Bind(R.id.set_video_show_scale_txt) TextView mVideoShowScaleTxt;
    @Bind(R.id.set_video_play_model_txt) TextView mVideoPlayModelTxt;
    @Bind(R.id.set_audio_play_model_txt) TextView mAudioPlayModelTxt;
    @Bind(R.id.set_start_play_model_txt) TextView mStartPlayModelTxt;
    @Bind(R.id.set_calendar_riqi_txt) TextView mCalendarDateTxt;
    @Bind(R.id.set_calendar_time_txt) TextView mCalendarTimeTxt;
    @Bind(R.id.set_calendar_nz_time_txt) TextView mCalendarAlertTimeTxt;
    @Bind(R.id.set_calendar_nz_frequency_txt) TextView mCalendarAlertFreqTxt;
    @Bind(R.id.set_start_dskgjpl_txt) TextView mAutoPowerFreqTxt;
    @Bind(R.id.set_select_language_txt) TextView mSelectLanguageTxt;
    @Bind(R.id.set_select_volume_txt) TextView mSelectVolumeTxt;
    @Bind(R.id.set_start_open_time_txt) TextView mOpenTimeTxt;
    @Bind(R.id.set_start_close_time_txt) TextView mCloseTimeTxt;
    @Bind(R.id.set_system_version_txt) TextView mSysVersionTxt;
    @Bind(R.id.set_breakpoint_play_sb) SwitchButton mBreakpointPlayBt;
    @Bind(R.id.set_bjyy_play_sb) SwitchButton mBgMusicBt;
    @Bind(R.id.set_subtitle_sb) SwitchButton mSubtitleBt;
    @Bind(R.id.set_show_channel_sb) SwitchButton mShowChannelBt;
    @Bind(R.id.set_calendar_nzzt_sb) SwitchButton mCalendarNzztBt;
    @Bind(R.id.set_calendar_timer_open_sb) SwitchButton mAutoPoweronBt;
    @Bind(R.id.set_calendar_timer_close_sb) SwitchButton mAutoPoweronOffBt;
    private TimePickerHelper mTimePickerHelper;
    private SelectDialog mSelectDialog;
    private List<String> mItemSelects = new ArrayList<>();
    private int mItemType;
    private DialogTips mDialogTips;
    // -----------系统配置--------------------

    @Bind(R.id.device_photo_model_type) ImageView mModelTypeImage;
    @Bind(R.id.device_media_sys_config_layout) LinearLayout mSysConfigLayout;
    @Bind(R.id.device_media_chart_recyclerview) RecyclerView mChartRecyclerView;
    @Bind(R.id.device_media_list_recyclerview) RecyclerView mListRecyclerView;
    @Bind(R.id.view_loading) LoadingView mLoadingView;
    @Bind(R.id.fragment_sys_config_tab) TextView fragmentSysConfigTab;
    @Bind(R.id.fragment_photo_image_tab) TextView fragmentPhotoImageTab;
    @Bind(R.id.fragment_photo_video_tab) TextView fragmentPhotoVideoTab;
    @Bind(R.id.fragment_photo_audio_tab) TextView fragmentPhotoAudioTab;
    @Bind(R.id.fragment_photo_all_tab) TextView fragmentPhotoAllTab;
    @Bind(R.id.remote_browser_frag_txt_upper) TextView mUpperView;

    /**
     * 显示模式
     */
    private MEDIA_SHOW_TYPE mShowModelType; // 模式
    private MEDIA_FILE_TYPE mShowMediaType; // 类别

    /**
     * 图表模式
     */
    private DeviceMediaAdapter mChartAdapter;
    /**
     * 列表模式
     */
    private DeviceListMediaAdapter mListAdapter;
    private int mDeletePosition;
    private boolean mSwitchDevice;

    private static final String PATH = AppContext.getInstance().getSString(R.string.device_the_txt);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_manager, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initData();
    }

    private void initView() {
        mChartRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mChartRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        getView().findViewById(R.id.remote_browser_frag_layout).setOnClickListener(this);
        mModelTypeImage.setOnClickListener(this);
        fragmentSysConfigTab.setOnClickListener(this);
        fragmentPhotoImageTab.setOnClickListener(this);
        fragmentPhotoVideoTab.setOnClickListener(this);
        fragmentPhotoAudioTab.setOnClickListener(this);
        fragmentPhotoAllTab.setOnClickListener(this);
        getView().findViewById(R.id.device_photo_choose_tab).setOnClickListener(this);

        // -------------系统配置----------------
        getView().findViewById(R.id.set_image_show_ratio_layout).setOnClickListener(mSysOnClickListener);
        getView().findViewById(R.id.set_image_play_time_layout).setOnClickListener(mSysOnClickListener);
        getView().findViewById(R.id.set_image_play_order_layout).setOnClickListener(mSysOnClickListener);
        getView().findViewById(R.id.set_video_show_scale_layout).setOnClickListener(mSysOnClickListener);
        getView().findViewById(R.id.set_video_play_model_layout).setOnClickListener(mSysOnClickListener);
        getView().findViewById(R.id.set_audio_play_model_layout).setOnClickListener(mSysOnClickListener);
        getView().findViewById(R.id.set_start_play_model_layout).setOnClickListener(mSysOnClickListener);
        getView().findViewById(R.id.set_calendar_nz_frequency_layout).setOnClickListener(mSysOnClickListener);
        getView().findViewById(R.id.set_start_dskgjpl_layout).setOnClickListener(mSysOnClickListener);
        getView().findViewById(R.id.set_calendar_layout).setOnClickListener(mSysOnClickListener);   // 日期
        getView().findViewById(R.id.set_calendar_time_layout).setOnClickListener(mSysOnClickListener);  // 时间
        getView().findViewById(R.id.set_calendar_nz_time_layout).setOnClickListener(mSysOnClickListener);  // 闹钟时间
        getView().findViewById(R.id.set_start_open_time_layout).setOnClickListener(mSysOnClickListener);  // 开机时间
        getView().findViewById(R.id.set_start_close_time_layout).setOnClickListener(mSysOnClickListener);  // 关机时间
        getView().findViewById(R.id.set_select_language_layout).setOnClickListener(mSysOnClickListener);  // 语言
        getView().findViewById(R.id.set_select_volume_layout).setOnClickListener(mSysOnClickListener);  // 音量
        getView().findViewById(R.id.set_system_reset_layout).setOnClickListener(mSysOnClickListener);  // 恢复出厂设置

        setSwitchListener(mBgMusicBt, SysConfigHelper.mBg_Music_key);
        setSwitchListener(mBreakpointPlayBt, SysConfigHelper.mBreakpointPlay_key);
        setSwitchListener(mSubtitleBt, SysConfigHelper.mSubtitle_key);
        setSwitchListener(mShowChannelBt, SysConfigHelper.mShowSpectrum_key);

        setSwitchListener(mBgMusicBt, SysConfigHelper.mBg_Music_key);
        setSwitchListener(mCalendarNzztBt, SysConfigHelper.mNz_states_key);
        setSwitchListener(mAutoPoweronBt, SysConfigHelper.mNz_dskj_key);
        setSwitchListener(mAutoPoweronOffBt, SysConfigHelper.mNz_dskj_off_key);

        mTimePickerHelper = new TimePickerHelper(getActivity());
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
        // 默认图表模式
        showModel(DeviceManager.getInstance().getShowType());

        SysConfigHelper.initTxt();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(EventBase eventBase) {
        String action = eventBase.getAction();
        if (action.equals(Constants.REFRESH_DEVICE_FILE)) {
            String tuue = (String) eventBase.getData();
            if(!TextUtils.isEmpty(tuue)) {
                if(mShowModelType == MEDIA_SHOW_TYPE.CHART) {
                    mChartAdapter.notifyDataSetChanged();
                } else {
                    mListAdapter.notifyDataSetChanged();
                }
            } else {
                refreshAdapter(mShowMediaType);
            }
        } else if(action.equals(Constants.SEND_DELETE_FILE_RESULT)) {
            // 删除结果
            boolean success = (boolean) eventBase.getData();
            if(success) {
                if(mShowModelType == MEDIA_SHOW_TYPE.CHART) {
                    // 图表模式
                    DeviceManager.getInstance().removeFile(mChartAdapter.getItem(mDeletePosition).getFileName());
                    mChartAdapter.remove(mDeletePosition);
                    mChartAdapter.notifyDataSetChanged();
                } else {
                    // 列表模式
                    DeviceManager.getInstance().removeFile(mListAdapter.getItem(mDeletePosition).getFileName());
                    mListAdapter.remove(mDeletePosition);
                    mListAdapter.notifyDataSetChanged();
                }
            }
            hideLoading();
        } else if(action.equals(Constants.SEND_DOWNLOAD_FILE_RESULT)) {
            // 下载结果
            DownloadInfo downloadInfo = (DownloadInfo) eventBase.getData();
            refresh(downloadInfo);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.fragment_sys_config_tab) {
            // 系统配置
            if (fragmentSysConfigTab.isSelected()) {
                return;
            }
            setSelectTab(0);
            mSysConfigLayout.setVisibility(View.VISIBLE);
            mChartRecyclerView.setVisibility(View.GONE);
            mListRecyclerView.setVisibility(View.GONE);
            mModelTypeImage.setEnabled(false);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    showLoading();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    initConfig();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    hideLoading();
                }
            }.execute();
            return;
        }
        if (DeviceManager.getInstance().isDownloading()) {
            ToastUtil.showToast(getActivity(), AppContext.getInstance().getSString(R.string.downloading_txt));
            return;
        }
        mSysConfigLayout.setVisibility(View.GONE);
        if(mShowModelType == MEDIA_SHOW_TYPE.CHART) {
            mChartRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mListRecyclerView.setVisibility(View.VISIBLE);
        }
        mModelTypeImage.setEnabled(true);
        switch (view.getId()) {
            case R.id.fragment_photo_image_tab:
                // 图片
                showFiles(MEDIA_FILE_TYPE.IMAGE, false);
                break;
            case R.id.fragment_photo_video_tab:
                // 视频
                showFiles(MEDIA_FILE_TYPE.VIDEO, false);
                break;
            case R.id.fragment_photo_audio_tab:
                // 音乐
                showFiles(MEDIA_FILE_TYPE.AUDIO, false);
                break;
            case R.id.fragment_photo_all_tab:
                // 全部
                showFiles(MEDIA_FILE_TYPE.ALL, false);
                break;
            case R.id.device_photo_choose_tab:
                // 切换设备
                mSwitchDevice = true;
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("type", Constants.MAIN_DEVICE_LIST);
                startActivity(intent);
                break;
            case R.id.device_photo_model_type:
                // 点击切换浏览模式
                new ChooseModePopView(getActivity(), this).showAsDropDown(view, 0, 0);
                break;
            case R.id.view_chart_mode:
                // 图表模式
                showModel(MEDIA_SHOW_TYPE.CHART);
                break;
            case R.id.view_list_mode:
                // 列表模式
                showModel(MEDIA_SHOW_TYPE.LIST);
                break;
            case R.id.remote_browser_frag_layout:
                // 点击列表模式的路径
                showLoading();
                DeviceManager.getInstance().prevRemoteCurrentPath();
                mUpperView.setText(PATH + mRemoteCurrentPath);
                break;
        }
    }

    private void showModel(MEDIA_SHOW_TYPE type) {
        mShowModelType = type;
        DeviceManager.getInstance().setShowType(type);
        if (type == MEDIA_SHOW_TYPE.CHART) {
            // 图表模式
            mListRecyclerView.setVisibility(View.GONE);
            mChartRecyclerView.setVisibility(View.VISIBLE);
            mModelTypeImage.setImageResource(R.drawable.mode_chrat_icon);
            if(mChartAdapter == null) {
                mChartAdapter = new DeviceMediaAdapter(getActivity());
                mChartRecyclerView.setAdapter(mChartAdapter);
                mChartAdapter.setOnInViewClickListener(R.id.item_device_media_download_txt, this);
                mChartAdapter.setOnInViewClickListener(R.id.item_device_media_delete_txt, this);
                mChartAdapter.setOnInViewClickListener(R.id.item_device_media_play_image, this);
                mChartAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View itemView, int pos) {
                        // item事件
                        FileInfo fileInfo = mChartAdapter.getItem(pos);
                        if (fileInfo.getFileType() == ActFileInfo.FILE_TYPE_DIRECTORY) {
                            // 点击文件夹
                            showLoading();
                            String requestPath = DeviceManager.getInstance().setRemoteCurrentPath(fileInfo.getFileName());
                            mUpperView.setText(PATH + requestPath);
                        } else if (fileInfo.getFileType() == ActFileInfo.FILE_TYPE_FILE) {
                            // 点击文件
                        }
                    }
                });
            }
        } else if (type == MEDIA_SHOW_TYPE.LIST) {
            // 列表模式
            mChartRecyclerView.setVisibility(View.GONE);
            mListRecyclerView.setVisibility(View.VISIBLE);
            mModelTypeImage.setImageResource(R.drawable.mode_list_icon);
            mUpperView.setText(PATH + mRemoteCurrentPath);
            if(mListAdapter == null) {
                mListAdapter = new DeviceListMediaAdapter(getActivity());
                mListRecyclerView.setAdapter(mListAdapter);
                mListAdapter.setOnInViewClickListener(R.id.item_menu_download_bt, this);
                mListAdapter.setOnInViewClickListener(R.id.item_menu_delete_bt, this);
                mListAdapter.setOnInViewClickListener(R.id.item_child_arrows_image, this);
                mListAdapter.setOnInViewClickListener(R.id.item_layout, this);
            }
        }
        if(mShowMediaType == null) {
            mShowMediaType = MEDIA_FILE_TYPE.IMAGE;
        }
        showFiles(mShowMediaType, true);
    }

    private void showFiles(MEDIA_FILE_TYPE type, boolean refresh) {
        mShowMediaType = type;
        if (type == MEDIA_FILE_TYPE.IMAGE) {
            // 图片
            if (!refresh && fragmentPhotoImageTab.isSelected()) {
                return;
            }
            setSelectTab(1);
        } else if (type == MEDIA_FILE_TYPE.VIDEO) {
            // 视频
            if (!refresh && fragmentPhotoVideoTab.isSelected()) {
                return;
            }
            setSelectTab(2);
        } else if (type == MEDIA_FILE_TYPE.AUDIO) {
            // 音乐
            if (!refresh && fragmentPhotoAudioTab.isSelected()) {
                return;
            }
            setSelectTab(3);
        } else if (type == MEDIA_FILE_TYPE.ALL) {
            // 全部
            if (!refresh && fragmentPhotoAllTab.isSelected()) {
                return;
            }
            setSelectTab(4);
        }
        refreshAdapter(type);
    }

    public void refreshData() {
        if(mSwitchDevice) {
            mShowMediaType = MEDIA_FILE_TYPE.IMAGE;
            setSelectTab(1);

            DeviceManager.getInstance().refreshBrowseFiles();
        }
        mSwitchDevice = false;
    }

    private AsyncTask<Void, Void, List<FileInfo>> mTask;

    private void refreshAdapter(final MEDIA_FILE_TYPE type) {
        mUpperView.setText(PATH + mRemoteCurrentPath);
        if(mTask != null) {
            mTask.cancel(true);
        }
        mTask = new AsyncTask<Void, Void, List<FileInfo>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoading();
            }

            @Override
            protected List<FileInfo> doInBackground(Void... voids) {
                // ActFileInfo to FileInfo
                List<FileInfo> result = new ArrayList<FileInfo>();
                int len = DeviceManager.getInstance().getRemoteDeviceFiles().size();
                for (int i = 0; i < len; i++) {
                    ActFileInfo actFileInfo = DeviceManager.getInstance().getRemoteDeviceFiles().get(i);

                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setmFileSize(actFileInfo.getFileSize());
                    fileInfo.setmModifyTime(actFileInfo.getModifyTime());
                    fileInfo.setFileName(actFileInfo.getFileName());
                    fileInfo.setFileType(actFileInfo.getFileType());
                    fileInfo.setPosition(i);
                    fileInfo.setType(type);

                    if(actFileInfo.getFileType() == ActFileInfo.FILE_TYPE_DIRECTORY) {
                        result.add(fileInfo);
                    } else {
                        if(PubUtils.isTypeFile(actFileInfo.getFileName(), type)) {
                            if(type == MEDIA_FILE_TYPE.ALL) {
                                MEDIA_FILE_TYPE type1 = PubUtils.getFileType(fileInfo.getFileName());
                                if(type1 == null) {
                                    continue;
                                }
                                fileInfo.setType(type1);
                            }
                            result.add(fileInfo);
                        }
                    }
                }

                List<FileInfo> temp = new ArrayList<>();
                len = result.size();
                for (int i = 0; i < len; i++) {
                    FileInfo fileInfo = result.get(i);
                    fileInfo.setPosition(i);
                    temp.add(fileInfo);
                    // 如果是视频文件
                    if(PubUtils.getFileType(fileInfo.getFileName()) == MEDIA_FILE_TYPE.VIDEO) {
                        DeviceManager.getInstance().addVideoBmpFileMaps(fileInfo.getFileName());
                    }
                }
                DeviceManager.getInstance().downloadBmpFiles();
                return temp;
            }

            @Override
            protected void onPostExecute(List<FileInfo> result) {
                super.onPostExecute(result);
                if(mShowModelType == MEDIA_SHOW_TYPE.CHART) {
                    // 图表模式
                    mChartAdapter.clear();
                    mChartAdapter.appendToList(result);
                    mChartAdapter.notifyDataSetChanged();
                } else {
                    // 列表模式
                    mListAdapter.clear();
                    mListAdapter.appendToList(result);
                    mListAdapter.notifyDataSetChanged();
                }
                if(DeviceManager.getInstance().isResposeFiles()) {
                    hideLoading();
                }
            }
        };
        mTask.execute();
    }

    private void setSelectTab(int id) {
        fragmentSysConfigTab.setSelected(false);
        fragmentPhotoImageTab.setSelected(false);
        fragmentPhotoVideoTab.setSelected(false);
        fragmentPhotoAudioTab.setSelected(false);
        fragmentPhotoAllTab.setSelected(false);
        switch (id) {
            case 0:
                fragmentSysConfigTab.setSelected(true);
                break;
            case 1:
                fragmentPhotoImageTab.setSelected(true);
                break;
            case 2:
                fragmentPhotoVideoTab.setSelected(true);
                break;
            case 3:
                fragmentPhotoAudioTab.setSelected(true);
                break;
            case 4:
                fragmentPhotoAllTab.setSelected(true);
                break;
        }
    }

    @Override
    public void OnClickListener(View parentV, View v, Integer position) {
        switch (v.getId()) {
            case R.id.item_device_media_download_txt:
                // 下载
                boolean isDownload = DeviceManager.getInstance().isDownloading();
                mChartAdapter.addDownload(position);
                if(!isDownload) {
                    DeviceManager.getInstance().startDownload();
                }
                break;
            case R.id.item_device_media_delete_txt:
                // 删除
                showLoading();
                mDeletePosition = position;
                DeviceManager.getInstance().deleteFile(mChartAdapter.getItem(mDeletePosition).getFileName());
                break;
            case R.id.item_device_media_play_image:
                // 播放
                FileInfo fileInfo = mChartAdapter.getItem(position);
                boolean play = addPlay(fileInfo);
                if(play) {
                    mChartAdapter.notifyItemChanged(position);
                }
                break;
            case R.id.item_menu_download_bt:
                // 列表模式下载
                isDownload = DeviceManager.getInstance().isDownloading();
                mListAdapter.addDownload(position);
                if(!isDownload) {
                    DeviceManager.getInstance().startDownload();
                }
                break;
            case R.id.item_menu_delete_bt:
                // 列表模式删除
                showLoading();
                mDeletePosition = position;
                fileInfo = mListAdapter.getItem(position);
                if(fileInfo.getFileType() == ActFileInfo.FILE_TYPE_DIRECTORY) {
                    // 文件夹
                    DeviceManager.getInstance().deleteDirectory(mListAdapter.getItem(position).getFileName());
                } else {
                    DeviceManager.getInstance().deleteFile(mListAdapter.getItem(position).getFileName());
                }
                break;
            case R.id.item_child_arrows_image:
                // 列表模式播放
                fileInfo = mListAdapter.getItem(position);
                play = addPlay(fileInfo);
                if(play) {
                    mListAdapter.notifyItemChanged(position);
                }
                break;
            case R.id.item_layout:
                // item事件
                fileInfo = mListAdapter.getItem(position);
                if (fileInfo.getFileType() == ActFileInfo.FILE_TYPE_DIRECTORY) {
                    // 点击文件夹
                    showLoading();
                    String requestPath = DeviceManager.getInstance().setRemoteCurrentPath(fileInfo.getFileName());
                    mUpperView.setText(PATH + requestPath);
                } else if (fileInfo.getFileType() == ActFileInfo.FILE_TYPE_FILE) {
                    // 点击文件
                }
                break;
        }
    }

    private boolean addPlay(FileInfo fileInfo) {
        if (fileInfo.getFileType() == ActFileInfo.FILE_TYPE_DIRECTORY) {
            // 如果是文件夹
            return false;
        }
//        if(DeviceManager.getInstance().isPlay(mRemoteCurrentPath + fileInfo.getFileName())) {
//            // 如果已经在播放里
//            return false;
//        }

        DeviceManager.getInstance().setPlayFile(fileInfo);
        return true;
    }

    private void showLoading() {
        mLoadingView.show();
    }

    private void hideLoading() {
        mLoadingView.hide();
    }

    private synchronized void refresh(DownloadInfo downloadInfo) {
        try {
            ProgressPieView pieView;
            if(mShowModelType == MEDIA_SHOW_TYPE.CHART) {
                // 图表模式
                pieView = (ProgressPieView) mChartRecyclerView.findViewWithTag("ProgressPieView" + downloadInfo.getFilePath());
            } else {
                // 列表模式
                pieView = (ProgressPieView) mListRecyclerView.findViewWithTag("ProgressPieView" + downloadInfo.getFilePath());
            }
            if (pieView != null) {
                if(pieView.getVisibility() == View.GONE) {
                    pieView.setVisibility(View.VISIBLE);
                }
                int state = downloadInfo.getState();
                if(state == 0) {
                    // 等待下载
                    pieView.setText(AppContext.getInstance().getSString(R.string.download_wait_txt));
                } else if(state == 1) {
                    // 下载中
                    int progress = Integer.parseInt(PubUtils.getSHCollagen(downloadInfo.getTotal(), downloadInfo.getProcessed()));
                    pieView.setProgress(progress);
                    pieView.setText((Math.round(progress * 100) * 1.0f / 100) + "%");
                } else if (state == -1) {
                    // 下载出错
                    pieView.setText(AppContext.getInstance().getSString(R.string.error_txt));
                } else if (state == 2) {
                    // 下载成功
                    pieView.setText(AppContext.getInstance().getSString(R.string.download_success_txt));
                    if(mShowModelType == MEDIA_SHOW_TYPE.CHART) {
                        // 图表模式
                        for(FileInfo fileInfo : mChartAdapter.getList()) {
                            String localPath = PubUtils.getDonwloadLocalPath(fileInfo.getFileName(), fileInfo.getType());
                            if(localPath.equals(downloadInfo.getFilePath())) {
                                PreferenceUtils.getInstance().putBoolen(localPath, true);
                                mChartAdapter.notifyItemChanged(fileInfo.getPosition());
                                return;
                            }
                        }
                    } else {
                        for(FileInfo fileInfo : mListAdapter.getList()) {
                            String localPath = PubUtils.getDonwloadLocalPath(fileInfo.getFileName(), fileInfo.getType());
                            if(localPath.equals(downloadInfo.getFilePath())) {
                                PreferenceUtils.getInstance().putBoolen(localPath, true);
                                mListAdapter.notifyItemChanged(fileInfo.getPosition());
                                return;
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
        // 日历日期
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String txt = DeviceManager.getInstance().getpropertiesValue(calendar_date);
                mCalendarDateTxt.setText(txt);
            }
        });
        // 日历时间
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String txt = DeviceManager.getInstance().getpropertiesValue(calendar_time);
                mCalendarTimeTxt.setText(txt);
            }
        });
        // 闹钟时间
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String txt = DeviceManager.getInstance().getpropertiesValue(calendar_alarm_time);
                mCalendarAlertTimeTxt.setText(txt);
            }
        });
        // 开机时间
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String txt = DeviceManager.getInstance().getpropertiesValue(sys_auto_on_time);
                mOpenTimeTxt.setText(txt);
            }
        });
        // 关机时间
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String txt = DeviceManager.getInstance().getpropertiesValue(sys_auto_off_time);
                mCloseTimeTxt.setText(txt);
            }
        });
        // 闹钟频率
        initProperties(calendar_alarm_freq, calendar_alarm_freq, mCalendarAlertFreqTxt, mAlarmFrequency);
        // 定时开关机频率
        initProperties(sys_auto_power_freq, sys_auto_power_freq, mAutoPowerFreqTxt, mAutoPowerRequency);
        // 当前语言
        final String language = DeviceManager.getInstance().getpropertiesValue(sys_lang_codes);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSelectLanguageTxt.setText(DeviceManager.getInstance().getLanguageStr(language));
            }
        });
        // 音量
        initProperties(sys_volume, sys_volume, mSelectVolumeTxt, mVolume);

        // 背景音乐-开关
        initSwitch(mBg_Music_key, mBgMusicBt);
        // 断点播放功能-开关
        initSwitch(mBreakpointPlay_key, mBreakpointPlayBt);
        // 字幕-开关
        initSwitch(mSubtitle_key, mSubtitleBt);
        // 显示频谱-开关
        initSwitch(mShowSpectrum_key, mShowChannelBt);
        // 闹钟状态
        initSwitch(mNz_states_key, mCalendarNzztBt);
        // 定时开机
        initSwitch(mNz_dskj_key, mAutoPoweronBt);
        // 定时关机
        initSwitch(mNz_dskj_off_key, mAutoPoweronOffBt);

        // 系统版本号
        final String version = DeviceManager.getInstance().getpropertiesValue(sys_ver);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSysVersionTxt.setText(version);
            }
        });
    }

    private void initSwitch(String key, final SwitchButton switchButton) {
        try {
            final String p = DeviceManager.getInstance().getpropertiesValue(key);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switchButton.setCheck(Integer.parseInt(p) == 0 ? false : true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化配置文件
    private void initProperties(String p_key, String sp_key, final TextView txtView, final String[] values) {
        try {
            final String p = DeviceManager.getInstance().getpropertiesValue(p_key);
            PreferenceUtils.getInstance().putInt(sp_key, Integer.parseInt(p));

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtView.setText(values[Integer.parseInt(p)]);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener mSysOnClickListener = new View.OnClickListener() {
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
                case R.id.set_calendar_nz_frequency_layout:
                    // 闹钟频率
                    mItemType = 18;
                    for (String str : mAlarmFrequency) {
                        mItemSelects.add(str);
                    }
                    break;
                case R.id.set_start_dskgjpl_layout:
                    // 定时开关机频率
                    mItemType = 19;
                    for (String str : mAutoPowerRequency) {
                        mItemSelects.add(str);
                    }
                    break;
                case R.id.set_calendar_layout:
                    // 日期
                    mTimePickerHelper.showDateDialog(new TimePickerHelper.OnPickerCallback() {
                        @Override
                        public void onTime(String time) {
                            DeviceManager.getInstance().sendSetdateDate(new String[] {"cmd", "Setdate", "Date", time});
                            DeviceManager.getInstance().setpropertiesValue(calendar_date, time);
                            mCalendarDateTxt.setText(time);
                        }
                    });
                    return;
                case R.id.set_calendar_time_layout:
                    // 时间
                    mTimePickerHelper.showTimeDialog(new TimePickerHelper.OnPickerCallback() {
                        @Override
                        public void onTime(String time) {
                            DeviceManager.getInstance().sendSetdateDate(new String[] {"cmd", "Settime", "Time", time});
                            DeviceManager.getInstance().setpropertiesValue(calendar_time, time);
                            mCalendarTimeTxt.setText(time);
                        }
                    });
                    return;
                case R.id.set_calendar_nz_time_layout:
                    // 闹钟时间
                    mTimePickerHelper.showTimeDialog(new TimePickerHelper.OnPickerCallback() {
                        @Override
                        public void onTime(String time) {
                            DeviceManager.getInstance().sendSetdateDate(new String[] {"cmd", "Settime", "Time", time});
                            DeviceManager.getInstance().setpropertiesValue(calendar_alarm_time, time);
                            mCalendarAlertTimeTxt.setText(time);
                        }
                    });
                    return;
                case R.id.set_start_open_time_layout:
                    // 开机时间
                    mTimePickerHelper.showTimeDialog(new TimePickerHelper.OnPickerCallback() {
                        @Override
                        public void onTime(String time) {
                            DeviceManager.getInstance().sendSetdateDate(new String[] {"cmd", "Settime", "Time", time});
                            DeviceManager.getInstance().setpropertiesValue(sys_auto_on_time, time);
                            mOpenTimeTxt.setText(time);
                        }
                    });
                    return;
                case R.id.set_start_close_time_layout:
                    // 关机时间
                    mTimePickerHelper.showTimeDialog(new TimePickerHelper.OnPickerCallback() {
                        @Override
                        public void onTime(String time) {
                            DeviceManager.getInstance().sendSetdateDate(new String[] {"cmd", "Settime", "Time", time});
                            DeviceManager.getInstance().setpropertiesValue(sys_auto_off_time, time);
                            mCloseTimeTxt.setText(time);
                        }
                    });
                    return;
                case R.id.set_select_language_layout:
                    // 选择语言
                    mItemType = 20;
                    String ls = DeviceManager.getInstance().getpropertiesValue(sys_lang_codes_all);
                    String[] languages = ls.split(";");
                    for (String str : languages) {
                        mItemSelects.add(str);
                    }
                    break;
                case R.id.set_select_volume_layout:
                    // 音量
                    mItemType = 21;
                    for (String str : mVolume) {
                        mItemSelects.add(str);
                    }
                    break;
                case R.id.set_system_reset_layout:
                    // 恢复出厂设置
                    mDialogTips = new DialogTips(getActivity());
                    mDialogTips.setMessage(AppContext.getInstance().getSString(R.string.set_system_reset));
                    mDialogTips.setOkListenner(new DialogTips.onDialogOkListenner() {
                        @Override
                        public void onClick() {
                            ActCommunication.getInstance().sendMsg(new String[]{"cmd", "ResetSetting"});
                        }
                    });
                    mDialogTips.setCancelListenner(null);
                    mDialogTips.setCanceledOnTouchOutside(false);
                    mDialogTips.show();
                    return;
            }
            showSelectDialog(mItemType, mItemSelects);
        }
    };

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
                    case 18:
                        // 闹钟频率
                        DeviceManager.getInstance().setpropertiesValue(calendar_alarm_freq, String.valueOf(position));
                        break;
                    case 19:
                        // 定时开关机频率
                        DeviceManager.getInstance().setpropertiesValue(sys_auto_power_freq, String.valueOf(position));
                        break;
                    case 20:
                        // 当前语言
                        DeviceManager.getInstance().setpropertiesValue(sys_lang_codes, itemSelects.get(position));
                        break;
                    case 21:
                        // 音量
                        DeviceManager.getInstance().setpropertiesValue(sys_volume, String.valueOf(position));
                        break;
                }
                initConfig();
            }
        });
    }

}
