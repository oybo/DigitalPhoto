package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import com.xyz.digital.photo.app.adapter.base.CommonAdapter;
import com.xyz.digital.photo.app.adapter.base.ViewHolder;
import com.xyz.digital.photo.app.bean.FileInfo;
import java.util.List;

/**
 * Created by O on 2017/3/18.
 */

public class DeviceListMediaAdapter extends CommonAdapter<FileInfo> {

    public DeviceListMediaAdapter(Context context, List<FileInfo> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(ViewHolder helper, FileInfo item) {

    }

}
