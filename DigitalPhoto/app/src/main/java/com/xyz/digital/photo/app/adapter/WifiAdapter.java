package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.ImageView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;

/**
 * Created by O on 2017/3/20.
 */

public class WifiAdapter extends BaseRecyclerAdapter<ScanResult> {

    private WifiManager mWifiManager;

    public WifiAdapter(Context ctx, WifiManager wifiManager) {
        super(ctx);
        this.mWifiManager = wifiManager;
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

        if(isConnectTheWifi(item)) {
            connectState.setImageResource(R.drawable.green_icon);
            connectState.setVisibility(View.VISIBLE);
        } else {
            connectState.setVisibility(View.GONE);
        }
    }

    private boolean isConnectTheWifi(ScanResult wifi) {
        boolean connect = false;
        try {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                if ((wifiInfo.getSSID().toString().replace("\"", "")).equals(wifi.SSID.toString().replace("\"", "")) &&
                        (wifiInfo.getBSSID().toString().replace("\"", "")).equals(wifi.BSSID.toString().replace("\"", ""))) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connect;
    }


}
