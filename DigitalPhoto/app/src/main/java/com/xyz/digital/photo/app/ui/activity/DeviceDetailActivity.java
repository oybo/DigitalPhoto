package com.xyz.digital.photo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actions.actfilemanager.ACTFileEventListener;
import com.actions.actfilemanager.ActFileInfo;
import com.actions.actfilemanager.ActFileManager;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.DeviceImageAdapter;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.util.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/4/5.
 */

public class DeviceDetailActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.device_detail_image_count_txt) TextView deviceDetailImageCountTxt;
    @Bind(R.id.device_detail_photo_rview) RecyclerView deviceDetailPhotoRview;

    @Bind(R.id.device_detail_disk_size_txt) TextView deviceDiskAllSize;
    @Bind(R.id.device_detail_disk_used_size_txt) TextView deviceDiskUsedSize;
    @Bind(R.id.device_detail_disk_usable_size_txt) TextView deviceDiskUsableSize;
    @Bind(R.id.device_detail_disk_usable_size_progressbar) ProgressBar deviceProgressBar;

    @Bind(R.id.device_u_detail_disk_size_txt) TextView usbDiskAllSize;
    @Bind(R.id.device_detail_u_disk_used_size_txt) TextView usbDiskUsedSize;
    @Bind(R.id.device_detail_u_disk_usable_size_txt) TextView usbDiskUsableSize;
    @Bind(R.id.device_detail_u_disk_usable_size_progressbar) ProgressBar usbProgressBar;

    private ActFileManager actFileManager = new ActFileManager();
    private static String mRemoteCurrentPath = "/";

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
        setRemoteCount(0);

        actFileManager.registerEventListener(new MyACTFileEventListener());
        actFileManager.connect(Constants.HOST_IP);
        actFileManager.browseFiles(mRemoteCurrentPath);

        mAdapter = new DeviceImageAdapter(this);
        deviceDetailPhotoRview.setAdapter(mAdapter);
    }

    private void setRemoteCount(int size) {
        String imangeCount = "共有<font color=\"#12B7F5\"> "+ size +" </font>个媒体文件";
        deviceDetailImageCountTxt.setText(Html.fromHtml(imangeCount));
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

    public class MyACTFileEventListener implements ACTFileEventListener {
        @Override
        public void onOperationProgression(int opcode, int processed, int total) {
        }

        @Override
        public void onUploadCompleted(String remotePath, String localPath, int result) {
        }

        @Override
        public void onDownloadCompleted(String remotePath, String localPath, int result) {
        }

        @Override
        public void onDeleteCompleted(String parentPath, int result) {
        }

        @Override
        public void onBrowseCompleted(Object filelist, String currentPath, int result) {
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                List<ActFileInfo> remoteFileList = (ArrayList) filelist;
                if(remoteFileList != null) {
                    setRemoteCount(remoteFileList.size());
                    mAdapter.clear();
                    mAdapter.appendToList(remoteFileList);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onDeleteDirectoryCompleted(String parentPath, int result) {
        }

        @Override
        public void onCreateDirectoryCompleted(String parentPath, int result) {
        }

        @Override
        public void onDisconnectCompleted(int result) {
        }
    }

}
