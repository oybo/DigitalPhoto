package com.xyz.digital.photo.app.adapter;

import android.content.Context;

import com.xyz.digital.photo.app.adapter.base.CommonAdapter;
import com.xyz.digital.photo.app.adapter.base.ViewHolder;
import com.xyz.digital.photo.app.bean.FolderBean;

import java.util.List;

/**
 * Created by O on 2017/3/18.
 */

public class DeviceListMediaAdapter extends CommonAdapter<FolderBean> {

    public DeviceListMediaAdapter(Context context, List<FolderBean> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(ViewHolder helper, FolderBean item) {

    }

}
