package com.xyz.digital.photo.app.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.LocalMediaAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.ui.BaseActivity;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/18.
 */

public class ShowImageListActivity extends BaseActivity {

    @Bind(R.id.show_imagelist_recyclerview) RecyclerView mRecyclerView;

    private LocalMediaAdapter mAdapter;

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

        mAdapter = new LocalMediaAdapter(this);
        mAdapter.appendToList(images);

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                MediaFileBean mediaFileBean = images.get(pos);
                if(mediaFileBean.getFileType() == MEDIA_FILE_TYPE.IMAGE) {
                    Intent intent = new Intent(ShowImageListActivity.this, PhotoViewActivity.class);
                    intent.putExtra("path", mediaFileBean.getFilePath());
                    intent.putExtra("title", mediaFileBean.getFileName());
                    intent.putExtra("date", mediaFileBean.getDate());
                    intent.putExtra("size", mediaFileBean.getSize());
                    startActivity(intent);
                } else {
                    String type = "audio/*";
                    if(mediaFileBean.getFileType() == MEDIA_FILE_TYPE.VIDEO) {
                        type = "video/*";
                    }

                    Intent it = new Intent(Intent.ACTION_VIEW);
                    it.setDataAndType(Uri.parse("file://" + mediaFileBean.getFilePath()), type);
                    startActivity(it);
                }
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
