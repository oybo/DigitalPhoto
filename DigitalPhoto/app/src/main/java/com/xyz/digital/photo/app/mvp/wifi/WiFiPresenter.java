package com.xyz.digital.photo.app.mvp.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by Administrator on 2017/4/14.
 */

public class WiFiPresenter implements WiFiContract.Presenter {

    private WiFiContract.View mView;

    // WifiManager对象
    private WifiManager mWifiManager;

    public WiFiPresenter(WiFiContract.View view) {
        mView = view;
        mView.setPresenter(this);

        mWifiManager = (WifiManager) mView._getActivity().getSystemService(Context.WIFI_SERVICE);
        registerBroadcast();
    }

    @Override
    public void scanWiFi() {
        mView.showLoading();
        openWifi();
        mWifiManager.startScan();
    }

    @Override
    public void connect(ScanResult wifi) {
        new WifiUtils(mView._getActivity()).connectWifiTest(wifi.SSID, "(oylx20160621)");
    }

    /**
     * 广播接收，监听网络
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // wifi已成功扫描到可用wifi。
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> wiFiResult = mWifiManager.getScanResults();
                mView.onCallbackDevice(wiFiResult);
            } //系统wifi的状态
            else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.d(TAG, "WiFi已启用");
                        mWifiManager.startScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.d(TAG, "Wifi已关闭");
                        break;
                }
            }

            mView.hideLoading();
        }
    };

    /**
     * 注册广播
     */
    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mView._getActivity().registerReceiver(mReceiver, filter);
    }

    public void unRegisterBroadcast() {
        mView._getActivity().unregisterReceiver((mReceiver));
    }

    /**
     * 打开wifi功能
     * true:打开成功；
     * false:打开失败
     */
    public boolean openWifi() {
        boolean bRet = true;
        if (!mWifiManager.isWifiEnabled()) {
            bRet = mWifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    /**
     * Function:关闭wifi
     *
     * @return<br>
     */
    public boolean closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            return mWifiManager.setWifiEnabled(false);
        }
        return false;
    }

}
