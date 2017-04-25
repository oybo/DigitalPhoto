package com.xyz.digital.photo.app.adapter;

import android.content.Context;

import com.actions.actfilemanager.ActFileInfo;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.manager.ImageLoadManager;

/**
 * Created by O on 2017/4/5.
 */

public class DeviceImageAdapter extends BaseRecyclerAdapter<ActFileInfo> {

    public DeviceImageAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_device_photo_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, ActFileInfo item) {

        ImageLoadManager.setImage("ftp://192.168.1.1/FBA96FF8592B259FED6B13D824A160C3.jpg", holder.getImageView(R.id.item_device_photo_image));

    }

}
