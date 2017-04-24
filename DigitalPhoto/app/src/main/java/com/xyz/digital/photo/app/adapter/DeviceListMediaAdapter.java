package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import com.actions.actfilemanager.ActFileInfo;
import com.xyz.digital.photo.app.adapter.base.CommonAdapter;
import com.xyz.digital.photo.app.adapter.base.ViewHolder;
import java.util.List;

/**
 * Created by O on 2017/3/18.
 */

public class DeviceListMediaAdapter extends CommonAdapter<ActFileInfo> {

    public DeviceListMediaAdapter(Context context, List<ActFileInfo> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(ViewHolder helper, ActFileInfo item) {

    }

}
