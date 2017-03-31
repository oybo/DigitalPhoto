package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.bean.WifiDevice;

/**
 * Created by O on 2017/3/20.
 */

public class WifiDeviceAdapter extends BaseRecyclerAdapter<WifiDevice> {

    public WifiDeviceAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_wifi_device_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, WifiDevice item) {

        holder.setText(R.id.item_wifi_device_title_txt, item.getName());

    }
}
