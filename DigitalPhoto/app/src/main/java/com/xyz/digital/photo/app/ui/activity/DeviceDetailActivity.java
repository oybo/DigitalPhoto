package com.xyz.digital.photo.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actions.actcommunication.ActCommunication;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.DeviceImageAdapter;
import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.PubUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/4/5.
 */

public class DeviceDetailActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.device_detail_image_count_txt) TextView deviceDetailImageCountTxt;
    @Bind(R.id.device_detail_photo_rview) RecyclerView deviceDetailPhotoRview;
    @Bind(R.id.device_detail_photo_loadingview) ProgressBar mLoadingView;

    @Bind(R.id.device_detail_disk_size_txt) TextView deviceDiskAllSizeTxt;
    @Bind(R.id.device_detail_disk_used_size_txt) TextView deviceDiskUsedSizeTxt;
    @Bind(R.id.device_detail_disk_usable_size_txt) TextView deviceDiskUsableSizeTxt;
    @Bind(R.id.device_detail_disk_usable_size_progressbar) ProgressBar deviceProgressBar;

    @Bind(R.id.device_u_detail_disk_size_txt) TextView usbDiskAllSizeTxt;
    @Bind(R.id.device_detail_u_disk_used_size_txt) TextView usbDiskUsedSizeTxt;
    @Bind(R.id.device_detail_u_disk_usable_size_txt) TextView usbDiskUsableSizeTxt;
    @Bind(R.id.device_detail_u_disk_usable_size_progressbar) ProgressBar usbProgressBar;

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
        EventBus.getDefault().register(this);

        setRemoteCount(0);
        setDiskSize("0", "0", "online");
        setUDiskSize("0", "0", "online");

        mAdapter = new DeviceImageAdapter(this);
        deviceDetailPhotoRview.setAdapter(mAdapter);

        EventBase eventBase = new EventBase();
        eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
        eventBase.setData("first");
        EventBus.getDefault().post(eventBase);

        // 请求内部存储剩余空间
        String[] msg = new String[]{"cmd", "reqNandInfo"};
        ActCommunication.getInstance().sendMsg(msg);
        // 请求U盘存储剩余空间
        msg = new String[]{"cmd", "reqUdiskInfo"};
        ActCommunication.getInstance().sendMsg(msg);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBase eventBase) {
        String action = eventBase.getAction();
        if (action.equals(Constants.REFRESH_DEVICE_FILE)) {
            mAdapter.clear();
            mAdapter.appendToList(DeviceManager.getInstance().getRemoteDeviceFiles());
            mAdapter.notifyDataSetChanged();
            setRemoteCount(DeviceManager.getInstance().getRemoteDeviceFiles().size());
            mLoadingView.setVisibility(View.GONE);
        } else if (action.equals(Constants.SEND_MNAD_INFO_ACTION)) {
            // 内部存储信息
            try {
                String[] result = (String[]) eventBase.getData();
                String size = result[3];
                String[] temp = size.split("/");
                String allSizeStr = temp[1];
                String usableSizeStr = temp[0];
                String stateStr = result[5];
                stateStr = "online";
                setDiskSize(allSizeStr, usableSizeStr, stateStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (action.equals(Constants.SEND_UDISK_INFO_ACTION)) {
            // U盘存储信息
            try {
                String[] result = (String[]) eventBase.getData();
                String size = result[3];
                String[] temp = size.split("/");
                String allSizeStr = temp[1];
                String usableSizeStr = temp[0];
                String stateStr = result[5];
                stateStr = "online";
                setUDiskSize(allSizeStr, usableSizeStr, stateStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setRemoteCount(int size) {
        String imangeCount = "共有<font color=\"#12B7F5\"> " + size + " </font>个媒体文件";
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

    private void setDiskSize(String allSizeStr, String usableSizeStr, String stateStr) {
        // 内部存储信息
        try {
            if ("online".equals(stateStr)) {
                long allSize = PubUtils.conversionSize(allSizeStr);
                long usableSize = PubUtils.conversionSize(usableSizeStr);
                // 总空间
                deviceDiskAllSizeTxt.setText(html2Str("本机存储容量:<font color=\"#DF6432\"> " + PubUtils.formatFileLen(allSize) + " </font>"));
                // 已用空间
                deviceDiskUsedSizeTxt.setText(html2Str("已用:<font color=\"#DF6432\"> " + PubUtils.formatFileLen(allSize - usableSize) + " </font>"));
                // 可用空间
                deviceDiskUsableSizeTxt.setText(html2Str("可用:<font color=\"#DF6432\"> " + PubUtils.formatFileLen(usableSize) + " </font>"));
                // 百分比
                deviceProgressBar.setMax((int) allSize);
                deviceProgressBar.setProgress((int) (allSize - usableSize));
            } else {
                deviceDiskAllSizeTxt.setText(html2Str("本机存储容量:<font color=\"#DF6432\"> 未发现设备 </font>"));
                deviceDiskUsedSizeTxt.setVisibility(View.GONE);
                deviceDiskUsableSizeTxt.setVisibility(View.GONE);
                deviceProgressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUDiskSize(String allSizeStr, String usableSizeStr, String stateStr) {
        // U盘内部存储信息
        try {
            if ("online".equals(stateStr)) {
                long allSize = PubUtils.conversionSize(allSizeStr);
                long usableSize = PubUtils.conversionSize(usableSizeStr);
                // 总空间
                usbDiskAllSizeTxt.setText(html2Str("U盘存储容量:<font color=\"#DF6432\"> " + PubUtils.formatFileLen(allSize) + " </font>"));
                // 已用空间
                usbDiskUsedSizeTxt.setText(html2Str("已用:<font color=\"#DF6432\"> " + PubUtils.formatFileLen(allSize - usableSize) + " </font>"));
                // 可用空间
                usbDiskUsableSizeTxt.setText(html2Str("可用:<font color=\"#DF6432\"> " + PubUtils.formatFileLen(usableSize) + " </font>"));
            } else {
                usbDiskAllSizeTxt.setText(html2Str("U盘存储容量:<font color=\"#DF6432\"> 未插入U盘 </font>"));
                usbDiskUsedSizeTxt.setVisibility(View.GONE);
                usbDiskUsableSizeTxt.setVisibility(View.GONE);
                usbProgressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Spanned html2Str(String str) {
        return Html.fromHtml(str);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
