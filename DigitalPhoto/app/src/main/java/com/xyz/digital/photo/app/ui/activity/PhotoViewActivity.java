/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.xyz.digital.photo.app.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.manager.ImageLoadManager;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.photoview.PhotoView;
import com.xyz.digital.photo.app.view.photoview.PhotoViewAttacher;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 图片详情页
 *
 * @author ouyangbo
 */
public class PhotoViewActivity extends BaseActivity implements OnClickListener {

    @Bind(R.id.photoview_image)
    PhotoView mImageView;
    @Bind(R.id.photoview_title_txt)
    TextView photoviewTitleTxt;
    @Bind(R.id.photoview_date_txt)
    TextView photoviewDateTxt;
    @Bind(R.id.photoview_size_txt)
    TextView photoviewSizeTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initData() {
        String path = getIntent().getStringExtra("path");
        ImageLoadManager.setImage(this, path, mImageView);

        photoviewTitleTxt.setText(getIntent().getStringExtra("title"));
        photoviewDateTxt.setText(getIntent().getStringExtra("date"));
        photoviewSizeTxt.setText(getIntent().getStringExtra("size"));
    }

    private void initView() {
        mImageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                finish();
            }
        });

        findViewById(R.id.photoview_delete_bt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photoview_delete_bt:
                ToastUtil.showToast(PhotoViewActivity.this, "删除？");
                break;
        }
    }

    @Override
    protected boolean translucentStatusBar() {
        return true;
    }

    @Override
    protected int getToolBarMenuView() {
        return 0;
    }

    @Override
    protected void onItemMenuSelected(MenuItem item) {

    }

}
