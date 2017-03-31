package com.xyz.digital.photo.app.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.ChooseImageAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.mvp.presenter.ChooseImagePresenter;
import com.xyz.digital.photo.app.mvp.view.ChooseImageView;
import com.xyz.digital.photo.app.ui.activity.ShowImageListActivity;
import com.xyz.digital.photo.app.util.ScreenUtils;
import com.xyz.digital.photo.app.view.ChooseModePopView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/18.
 * 本地文件
 */

public class PhotoFragment extends Fragment implements Toolbar.OnMenuItemClickListener, ChooseImageView {

    @Bind(R.id.fragment_localfile_toolbar) Toolbar mToolbar;
    @Bind(R.id.fragment_localfile_recyclerview) RecyclerView mRecyclerView;

    private ChooseImagePresenter mPresenter;
    private ChooseImageAdapter mAdapter;
    private HashMap<String, List<MediaFileBean>> mMediaFiles;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_file, container, false);
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
        mToolbar.setTitle("");
        mToolbar.inflateMenu(R.menu.menu_local_file_view);
        mToolbar.setOnMenuItemClickListener(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    }

    private void initData() {
        mAdapter = new ChooseImageAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mPresenter = new ChooseImagePresenter(this);
        mPresenter.loadMediaFiles();

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                String folderName = mAdapter.getItem(pos).getFolderName();
                List<MediaFileBean> childList = mMediaFiles.get(folderName);

                Intent intent = new Intent(getActivity(), ShowImageListActivity.class);
                intent.putExtra("title", folderName);
                intent.putExtra("data", (Serializable) childList);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_local_file_look:
                // 浏览模式
                new ChooseModePopView(getActivity(), mMoreCallBack).showAsDropDown(mToolbar, 0, -ScreenUtils.dpToPxInt(10));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener mMoreCallBack = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.view_choose_mode_catalog:
                    // 目录模式
                    break;
                case R.id.view_choose_mode_dir:
                    // 浏览模式
                    break;
            }
        }
    };

    @Override
    public void callbackMedias(HashMap<String, List<MediaFileBean>> images) {
        mMediaFiles = images;

        mAdapter.appendToList(mPresenter.subGroupOfMedia(images));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public Context _getActivity() {
        return getActivity();
    }
}
