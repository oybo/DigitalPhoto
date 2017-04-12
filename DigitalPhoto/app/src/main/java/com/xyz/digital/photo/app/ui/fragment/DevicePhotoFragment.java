package com.xyz.digital.photo.app.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.DeviceMediaAdapter;
import com.xyz.digital.photo.app.adapter.FolderAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;
import com.xyz.digital.photo.app.mvp.device.media.DeviceMediaContract;
import com.xyz.digital.photo.app.mvp.device.media.DeviceMediaPresenter;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.ui.activity.MainActivity;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.ChooseModePopView;
import com.xyz.digital.photo.app.view.DividerItemDecoration;
import com.xyz.digital.photo.app.view.LoadingView;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/4/12.
 */

public class DevicePhotoFragment extends BaseFragment implements View.OnClickListener, DeviceMediaContract.View, BaseRecyclerAdapter
        .onInternalClickListener {

    @Bind(R.id.device_photo_model_type) ImageView mModelTypeImage;
    @Bind(R.id.device_media_chart_recyclerview) RecyclerView mChartRecyclerView;
    @Bind(R.id.device_media_list_recyclerview) RecyclerView mListRecyclerView;
    @Bind(R.id.view_loading) LoadingView mLoadingView;
    @Bind(R.id.fragment_photo_image_tab) TextView fragmentPhotoImageTab;
    @Bind(R.id.fragment_photo_video_tab) TextView fragmentPhotoVideoTab;
    @Bind(R.id.fragment_photo_audio_tab) TextView fragmentPhotoAudioTab;
    @Bind(R.id.fragment_photo_play_tab) TextView fragmentPhotoPlayTab;

    private DeviceMediaContract.Presenter mPresenter;

    private DeviceMediaAdapter mChartAdapter;
    private FolderAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_device_manager, container, false);
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
        mChartRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChartRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mListRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        mModelTypeImage.setOnClickListener(this);
        fragmentPhotoImageTab.setOnClickListener(this);
        fragmentPhotoVideoTab.setOnClickListener(this);
        fragmentPhotoAudioTab.setOnClickListener(this);
        fragmentPhotoPlayTab.setOnClickListener(this);
        getView().findViewById(R.id.device_photo_choose_tab).setOnClickListener(this);
    }

    private void initData() {
        mChartAdapter = new DeviceMediaAdapter(getActivity());
        mChartRecyclerView.setAdapter(mChartAdapter);

        mListAdapter = new FolderAdapter(getActivity());
        mListRecyclerView.setAdapter(mListAdapter);

        mPresenter = new DeviceMediaPresenter(this);
        // 默认图表模式
        mPresenter.showType(MEDIA_SHOW_TYPE.CHART);

        mChartAdapter.setOnInViewClickListener(R.id.item_device_media_download_txt, this);
        mChartAdapter.setOnInViewClickListener(R.id.item_device_media_delete_txt, this);
        mChartAdapter.setOnInViewClickListener(R.id.item_device_media_play_image, this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_photo_image_tab:
                // 图片
                if(fragmentPhotoImageTab.isSelected()) {
                    return;
                }
                mPresenter.showMediaFiles(MEDIA_FILE_TYPE.IMAGE);
                setSelectTab(1);
                break;
            case R.id.fragment_photo_video_tab:
                // 视频
                if(fragmentPhotoVideoTab.isSelected()) {
                    return;
                }
                mPresenter.showMediaFiles(MEDIA_FILE_TYPE.VIDEO);
                setSelectTab(2);
                break;
            case R.id.fragment_photo_audio_tab:
                // 音乐
                if(fragmentPhotoAudioTab.isSelected()) {
                    return;
                }
                mPresenter.showMediaFiles(MEDIA_FILE_TYPE.AUDIO);
                setSelectTab(3);
                break;
            case R.id.fragment_photo_play_tab:
                // 播放中
                if(fragmentPhotoPlayTab.isSelected()) {
                    return;
                }
                mPresenter.showMediaFiles(MEDIA_FILE_TYPE.PLAY);
                setSelectTab(4);
                break;
            case R.id.device_photo_choose_tab:
                // 切换设备
                startActivity(new Intent(getActivity(), MainActivity.class));
                break;
            case R.id.device_photo_model_type:
                // 点击切换浏览模式
                new ChooseModePopView(getActivity(), this).showAsDropDown(view, 0, 0);
                break;
            case R.id.view_chart_mode:
                // 图表模式
                mPresenter.showType(MEDIA_SHOW_TYPE.CHART);
                break;
            case R.id.view_list_mode:
                // 列表模式
                mPresenter.showType(MEDIA_SHOW_TYPE.LIST);
                break;
        }
    }

    private void setSelectTab(int id) {
        fragmentPhotoImageTab.setSelected(false);
        fragmentPhotoVideoTab.setSelected(false);
        fragmentPhotoAudioTab.setSelected(false);
        fragmentPhotoPlayTab.setSelected(false);
        switch (id) {
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
                fragmentPhotoPlayTab.setSelected(true);
                break;
        }
    }

    @Override
    public void OnClickListener(View parentV, View v, Integer position) {
        switch (v.getId()) {
            case R.id.item_device_media_download_txt:

                ToastUtil.showToast(getActivity(), "下载");
                break;
            case R.id.item_device_media_delete_txt:

                ToastUtil.showToast(getActivity(), "删除");
                break;
            case R.id.item_device_media_play_image:

                ToastUtil.showToast(getActivity(), "播放");
                break;
        }
    }

    @Override
    public Activity _getActivity() {
        return getActivity();
    }

    @Override
    public void onCallbackMediasByList(final boolean isRefreshModel, final HashMap<String, List<MediaFileBean>> files) {
        // 列表模式
        try {
//            mListImages = images;
            mListAdapter.clear();
            mListAdapter.appendToList(mPresenter.subGroupOfMedia(files));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListAdapter.notifyDataSetChanged();
                    if (isRefreshModel) {
                        setSelectTab(1);
                        mChartRecyclerView.setVisibility(View.GONE);
                        mListRecyclerView.setVisibility(View.VISIBLE);
                        mModelTypeImage.setImageResource(R.drawable.mode_list_icon);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCallbackMediasByChart(final boolean isRefreshModel, List<MediaFileBean> files) {
        // 图表模式
        try {
            mChartAdapter.clear();
            mChartAdapter.appendToList(files);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChartAdapter.notifyDataSetChanged();
                    if (isRefreshModel) {
                        setSelectTab(1);
                        mListRecyclerView.setVisibility(View.GONE);
                        mChartRecyclerView.setVisibility(View.VISIBLE);
                        mModelTypeImage.setImageResource(R.drawable.mode_chrat_icon);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPresenter(DeviceMediaContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {
        mLoadingView.show();
    }

    @Override
    public void hideLoading() {
        mLoadingView.hide();
    }

}
