package com.xyz.digital.photo.app.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.ChildImageAdapter;
import com.xyz.digital.photo.app.adapter.FolderAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.mvp.Photo.PhotoContract;
import com.xyz.digital.photo.app.mvp.Photo.PhotoPresenter;
import com.xyz.digital.photo.app.ui.activity.ShowImageListActivity;
import com.xyz.digital.photo.app.util.ScreenUtils;
import com.xyz.digital.photo.app.view.ChooseModePopView;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/18.
 * 本地文件
 */

public class PhotoFragment extends Fragment implements Toolbar.OnMenuItemClickListener, PhotoContract.View {

    @Bind(R.id.fragment_localfile_toolbar) Toolbar mToolbar;
    @Bind(R.id.fragment_media_chart_recyclerview) RecyclerView mChartRecyclerView;
    @Bind(R.id.fragment_media_list_recyclerview) RecyclerView mListRecyclerView;

    private PhotoContract.Presenter mPresenter;

    private ChildImageAdapter mChartAdapter;
    private FolderAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
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
        mToolbar.setTitle("图表");
        mToolbar.inflateMenu(R.menu.menu_photo_view);
        mToolbar.setOnMenuItemClickListener(this);
        setTitleListener();

        mChartRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mListRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    }

    private void setTitleListener() {
        try {
            //获取成员变量
            Field f = mToolbar.getClass().getDeclaredField("mTitleTextView");
            //设置可访问
            f.setAccessible(true);
            //获取到mSubtitleTextView的实例
            //这里使用final是为了方便下面在匿名内部类里使用
            //传入的是toolbar实例
            final TextView tv = (TextView)f.get(mToolbar);
            //为mSubtitleTextView设置省略号显示在开头部位
            tv.setEllipsize(TextUtils.TruncateAt.START);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 点击标题
                    new ChooseModePopView(getActivity(), mMoreCallBack).showAsDropDown(mToolbar, 0, -ScreenUtils.dpToPxInt(10));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        mPresenter = new PhotoPresenter(this);
        // 默认图表模式
        mPresenter.showType(PhotoContract.MEDIA_SHOW_TYPE.LIST);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_image:
                // 图片
                mPresenter.showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.IMAGE);
                break;
            case R.id.menu_video:
                // 视频
                mPresenter.showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.VIDEO);
                break;
            case R.id.menu_audio:
                // 音乐
                mPresenter.showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.AUDIO);
                break;
            case R.id.menu_choose:
                // 选择
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener mMoreCallBack = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.view_chart_mode:
                    // 图表模式
                    mPresenter.showType(PhotoContract.MEDIA_SHOW_TYPE.CHART);
                    break;
                case R.id.view_list_mode:
                    // 列表模式
                    mPresenter.showType(PhotoContract.MEDIA_SHOW_TYPE.LIST);
                    break;
            }
        }
    };

    @Override
    public Activity _getActivity() {
        return getActivity();
    }

    @Override
    public void onCallbackMediasByList(final HashMap<String, List<MediaFileBean>> images) {
        // 列表模式
        mChartRecyclerView.setVisibility(View.GONE);
        mListRecyclerView.setVisibility(View.VISIBLE);
        mToolbar.setTitle("列表模式");

        if(mListAdapter == null) {
            mListAdapter = new FolderAdapter(getActivity());
            mListRecyclerView.setAdapter(mListAdapter);
            mListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, int pos) {
                    String folderName = mListAdapter.getItem(pos).getFolderName();
                    List<MediaFileBean> childList = images.get(folderName);

                    Intent intent = new Intent(getActivity(), ShowImageListActivity.class);
                    intent.putExtra("title", folderName);
                    intent.putExtra("data", (Serializable) childList);
                    startActivity(intent);
                }
            });
        }
        mListAdapter.clear();
        mListAdapter.appendToList(mPresenter.subGroupOfMedia(images));
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCallbackMediasByChart(List<MediaFileBean> files) {
        // 图表模式
        mListRecyclerView.setVisibility(View.GONE);
        mChartRecyclerView.setVisibility(View.VISIBLE);
        mToolbar.setTitle("图表模式");

        if(mChartAdapter == null) {
            mChartAdapter = new ChildImageAdapter(_getActivity());
            mChartRecyclerView.setAdapter(mChartAdapter);
            mChartAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, int pos) {

                }
            });
        }
        mChartAdapter.clear();
        mChartAdapter.appendToList(files);
        mChartAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(PhotoContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
