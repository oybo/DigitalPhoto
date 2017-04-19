package com.xyz.digital.photo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.DeviceImageAdapter;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.util.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/4/5.
 */

public class DeviceDetailActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.device_detail_image_count_txt) TextView deviceDetailImageCountTxt;
    @Bind(R.id.device_detail_photo_rview) RecyclerView deviceDetailPhotoRview;

    private DeviceImageAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initView() {
        initTopBarOnlyTitle("设备详情");

        deviceDetailPhotoRview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        deviceDetailPhotoRview.setHasFixedSize(true);

        findViewById(R.id.device_detail_manager_bt).setOnClickListener(this);
    }

    private void initData() {
        String imangeCount = "共有<font color=\"#12B7F5\">10</font>个媒体文件";
        deviceDetailImageCountTxt.setText(Html.fromHtml(imangeCount));

        mAdapter = new DeviceImageAdapter(this);
        mAdapter.append("http://img0.imgtn.bdimg.com/it/u=461866017,3145489827&fm=214&gp=0.jpg");
        mAdapter.append("http://www.zhiyinlady.com/d/file/20170322/c778c82d7dc60d1848ef420c49f60cde.jpg");
        mAdapter.append("http://img15.3lian.com/2015/f2/146/d/154.jpg");
        mAdapter.append("http://img.ctoy.com.cn/vipcom/chaoyatoys/201182992333144.jpg");
        mAdapter.append("http://img4.imgtn.bdimg.com/it/u=1942403081,3970771863&fm=11&gp=0.jpg");
        mAdapter.append("http://img5.niutuku.com/phone/1301/1044/1044-niutuku.com-298018.jpg");
        deviceDetailPhotoRview.setAdapter(mAdapter);
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
            case R.id.device_detail_manager_bt:
                Intent intent = new Intent(DeviceDetailActivity.this, MainActivity.class);
                intent.putExtra("type", Constants.MAIN_DEVICE_POHOTO_MANAGER);
                startActivity(intent);
                break;
        }
    }
}
