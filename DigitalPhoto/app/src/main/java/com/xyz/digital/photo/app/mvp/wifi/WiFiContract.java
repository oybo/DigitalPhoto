package com.xyz.digital.photo.app.mvp.wifi;

import android.app.Activity;
import android.net.wifi.ScanResult;

import com.xyz.digital.photo.app.mvp.BasePresenter;
import com.xyz.digital.photo.app.mvp.BaseView;

import java.util.List;

/**
 * Created by O on 2017/3/31.
 */

public interface WiFiContract {

    interface View extends BaseView<Presenter> {
        Activity _getActivity();
        void onCallbackDevice(List<ScanResult> wifiP2pDevices);
    }

    interface Presenter extends BasePresenter {
        void unRegisterBroadcast();
        void scanWiFi();
        void connect(ScanResult wifi);
    }

}
