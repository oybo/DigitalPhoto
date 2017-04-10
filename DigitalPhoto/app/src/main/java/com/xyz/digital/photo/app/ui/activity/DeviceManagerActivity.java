package com.xyz.digital.photo.app.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.DeviceMediaAdapter;
import com.xyz.digital.photo.app.adapter.FolderAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;
import com.xyz.digital.photo.app.mvp.device.media.DeviceMediaContract;
import com.xyz.digital.photo.app.mvp.device.media.DeviceMediaPresenter;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.ChooseModePopView;
import com.xyz.digital.photo.app.view.DividerItemDecoration;
import com.xyz.digital.photo.app.view.LoadingView;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;

/**
 * Created by O on 2017/4/5.
 */

public class DeviceManagerActivity extends BaseActivity implements View.OnClickListener, DeviceMediaContract.View, BaseRecyclerAdapter.onInternalClickListener {

    @Bind(R.id.device_photo_model_type) ImageView mModelTypeImage;
    @Bind(R.id.device_photo_tablayout) TabLayout mTabLayout;
    @Bind(R.id.device_media_chart_recyclerview) RecyclerView mChartRecyclerView;
    @Bind(R.id.device_media_list_recyclerview) RecyclerView mListRecyclerView;
    @Bind(R.id.view_loading) LoadingView mLoadingView;

    private DeviceMediaContract.Presenter mPresenter;

    private DeviceMediaAdapter mChartAdapter;
    private FolderAdapter mListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);

        initView();
        initData();
    }

    private void initView() {
        mModelTypeImage.setOnClickListener(this);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                switch (pos) {
                    case 0:
                        // 图片
                        tab.select();
                        mPresenter.showMediaFiles(MEDIA_FILE_TYPE.IMAGE);
                        break;
                    case 1:
                        // 视频
                        tab.select();
                        mPresenter.showMediaFiles(MEDIA_FILE_TYPE.VIDEO);
                        break;
                    case 2:
                        // 音乐
                        tab.select();
                        mPresenter.showMediaFiles(MEDIA_FILE_TYPE.AUDIO);
                        break;
                    case 3:
                        // 播放中
                        tab.select();
                        mPresenter.showMediaFiles(MEDIA_FILE_TYPE.PLAY);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mChartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChartRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mListRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    }

    private void initData() {
        mChartAdapter = new DeviceMediaAdapter(this);
        mChartRecyclerView.setAdapter(mChartAdapter);

        mListAdapter = new FolderAdapter(this);
        mListRecyclerView.setAdapter(mListAdapter);

        mPresenter = new DeviceMediaPresenter(this);
        // 默认图表模式
        mPresenter.showType(MEDIA_SHOW_TYPE.CHART);

        mChartAdapter.setOnInViewClickListener(R.id.item_device_media_download_txt, this);
        mChartAdapter.setOnInViewClickListener(R.id.item_device_media_delete_txt, this);
        mChartAdapter.setOnInViewClickListener(R.id.item_device_media_play_image, this);
    }

    @Override
    protected int getToolBarMenuView() {
        return 0;
    }

    @Override
    protected void onItemMenuSelected(MenuItem item) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.device_photo_model_type:
                // 点击切换浏览模式
                new ChooseModePopView(DeviceManagerActivity.this, this).showAsDropDown(view, 0, 0);
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

    @Override
    public void OnClickListener(View parentV, View v, Integer position) {
        switch (v.getId()) {
            case R.id.item_device_media_download_txt:

                ToastUtil.showToast(DeviceManagerActivity.this, "下载");
                break;
            case R.id.item_device_media_delete_txt:

                ToastUtil.showToast(DeviceManagerActivity.this, "删除");
                break;
            case R.id.item_device_media_play_image:

                ToastUtil.showToast(DeviceManagerActivity.this, "播放");
                break;
        }
    }

    @Override
    public Activity _getActivity() {
        return this;
    }

    @Override
    public void onCallbackMediasByList(final boolean isRefreshModel, final HashMap<String, List<MediaFileBean>> files) {
        // 列表模式
        try {
//            mListImages = images;
            mListAdapter.clear();
            mListAdapter.appendToList(mPresenter.subGroupOfMedia(files));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListAdapter.notifyDataSetChanged();
                    if(isRefreshModel) {
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChartAdapter.notifyDataSetChanged();
                    if(isRefreshModel) {
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
