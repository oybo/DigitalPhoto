package com.xyz.digital.photo.app.mvp.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import com.xyz.digital.photo.app.mvp.wifi.connecter.Wifi;

/**
 * Created by Administrator on 2017/4/14.
 */

public class WiFiPresenter implements WiFiContract.Presenter {

    private WiFiContract.View mView;

    public WiFiPresenter(WiFiContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public boolean connect(final WifiManager mWifiManager, final ScanResult mScanResult) {
        final String security = Wifi.ConfigSec.getScanResultSecurity(mScanResult);
        final WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager, mScanResult, security);

        boolean connResult = false;

        if(config == null) {
            boolean mIsOpenNetwork = false;
            if(Wifi.ConfigSec.isOpenNetwork(Wifi.ConfigSec.getScanResultSecurity(mScanResult))) {
                mIsOpenNetwork = true;
            }
            int mNumOpenNetworksKept =  Settings.Secure.getInt(mView._getActivity().getContentResolver(),
                    Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT, 10);
            // 没有配置过
            if(mIsOpenNetwork) {
                connResult = Wifi.connectToNewNetwork(mView._getActivity(), mWifiManager, mScanResult, null, mNumOpenNetworksKept);
            } else {
                connResult = Wifi.connectToNewNetwork(mView._getActivity(), mWifiManager, mScanResult, "12345678", mNumOpenNetworksKept);
            }
        } else {
            // 有配置过
            final boolean isCurrentNetwork_ConfigurationStatus = config.status == WifiConfiguration.Status.CURRENT;
            final WifiInfo info = mWifiManager.getConnectionInfo();
            final boolean isCurrentNetwork_WifiInfo = info != null
                    && android.text.TextUtils.equals(info.getSSID(), mScanResult.SSID)
                    && android.text.TextUtils.equals(info.getBSSID(), mScanResult.BSSID);
            if(isCurrentNetwork_ConfigurationStatus || isCurrentNetwork_WifiInfo) {
                // 当前已经连接了该热点
                connResult = true;
            } else {
                if(config != null) {
                    connResult = Wifi.connectToConfiguredNetwork(mView._getActivity(), mWifiManager, config, false);
                }
            }
        }

        return connResult;
    }

}
