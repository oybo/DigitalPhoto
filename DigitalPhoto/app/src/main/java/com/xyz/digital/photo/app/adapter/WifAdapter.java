package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.View;
import android.widget.ImageView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.mvp.wifi.WifiUtils;

/**
 * Created by O on 2017/3/20.
 */

public class WifAdapter extends BaseRecyclerAdapter<ScanResult> {

    public WifAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_wifi_device_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, ScanResult item) {

        holder.setText(R.id.item_wifi_device_position_txt, String.valueOf(position + 1));

        holder.setText(R.id.item_wifi_device_name_txt, item.SSID);

        ImageView connectState = holder.getImageView(R.id.item_wifi_device_status_txt);
        if(WifiUtils.isConnectTheWifi(item)) {
            connectState.setImageResource(R.drawable.green_icon);
            connectState.setVisibility(View.VISIBLE);
        } else {
            connectState.setVisibility(View.GONE);
        }

        // 判断是否可用
//
//        if(item.status == WifiP2pDevice.AVAILABLE) {
//            holder.setImageResouce(R.id.item_wifi_device_status_txt, R.drawable.green_icon);
//        } else if(item.status == WifiP2pDevice.UNAVAILABLE) {
//            holder.setImageResouce(R.id.item_wifi_device_status_txt, R.drawable.read_icon);
//        }
//
//        // 判断连接状态
//        TextView connectTxt = holder.getTextView(R.id.item_wifi_device_is_connect_txt);
//        if(item.status == WifiP2pDevice.CONNECTED) {
//            connectTxt.setVisibility(View.VISIBLE);
//        } else {
//            connectTxt.setVisibility(View.GONE);
//        }
//

//        holder.setText(R.id.item_wifi_device_address_txt, item.level);

    }
}
