package com.xyz.digital.photo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.ChildImageAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.ui.BaseActivity;

import java.io.Serializable;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/18.
 */

public class ShowImageListActivity extends BaseActivity {

    @Bind(R.id.show_imagelist_recyclerview) RecyclerView mRecyclerView;

    private ChildImageAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initData() {
        final List<MediaFileBean> images = (List<MediaFileBean>) getIntent().getSerializableExtra("data");

        mAdapter = new ChildImageAdapter(this);
        mAdapter.appendToList(images);

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                Intent intent = new Intent(ShowImageListActivity.this, PhotoViewActivity.class);
                intent.putExtra("paths", (Serializable) images);
                intent.putExtra("curIndex", pos);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        initTopBarOnlyTitle(getIntent().getStringExtra("title"));

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    }

    @Override
    protected boolean translucentStatusBar() {
        return false;
    }

    @Override
    protected int getToolBarMenuView() {
        return 0;
    }

    @Override
    protected void onItemMenuSelected(MenuItem item) {

    }
}
