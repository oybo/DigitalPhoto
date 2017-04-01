package com.xyz.digital.photo.app.mvp.device;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;

import com.xyz.digital.photo.app.mvp.BasePresenter;
import com.xyz.digital.photo.app.mvp.BaseView;

import java.util.List;

/**
 * Created by O on 2017/3/31.
 */

public interface DeviceContract {

    interface View extends BaseView<Presenter> {
        Activity _getActivity();
        void onCallbackDevice(List<WifiP2pDevice> wifiP2pDevices);
    }

    interface Presenter extends BasePresenter {
        void scanWifiDevice();
    }

    interface OnScanDeviceListener {
        void onDevice(List list);
    }

}
