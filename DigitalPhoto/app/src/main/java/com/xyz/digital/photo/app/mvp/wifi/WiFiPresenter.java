package com.xyz.digital.photo.app.mvp.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.xyz.digital.photo.app.util.ToastUtil;

import java.util.List;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by Administrator on 2017/4/14.
 */

public class WiFiPresenter implements WiFiContract.Presenter {

    private WiFiContract.View mView;
    private AsyncTask<Void, Void, Boolean> mConnectTask;

    private WifiUtils mWifiUtils;

    public WiFiPresenter(WiFiContract.View view) {
        mView = view;
        mView.setPresenter(this);

        mWifiUtils = new WifiUtils();
        registerBroadcast();
    }

    @Override
    public void scanWiFi() {
        WifiUtils.openWifi();
        WifiUtils.startScan();
    }

    @Override
    public void connect(final ScanResult wifi) {
        if(WifiUtils.isConnectTheWifi(wifi)) {
            mView.onCallbackConnect();
            return;
        }
        if(mConnectTask != null) {
            mConnectTask.cancel(true);
        }
        mConnectTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mView.showLoading();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean success = WifiUtils.connectWifi(wifi.SSID, "12345678", WifiUtils.WifiCipherType.WIFICIPHER_WPA);
                return success;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                ToastUtil.showToast(mView._getActivity(), success ? "连接成功" : "连接失败");

                mView.hideLoading();
                if(success) {
                    mView.onCallbackConnect();
                }
            }
        };
        mConnectTask.execute();
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
                List<ScanResult> wiFiResult = WifiUtils.getScanResults();
                mView.onCallbackDevice(wiFiResult);
                if(wiFiResult.size() == 0) {
                    // 重新扫描
                    scanWiFi();
                }
            } //系统wifi的状态
            else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.d(TAG, "WiFi已启用");
                        WifiUtils.startScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.d(TAG, "Wifi已关闭");
                        break;
                }
            }
            else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                // wifi连接状态更改
                mView.notifyDataSetChanged();
            }
        }
    };

    /**
     * 注册广播
     */
    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mView._getActivity().registerReceiver(mReceiver, filter);
    }

    public void unRegisterBroadcast() {
        mView._getActivity().unregisterReceiver((mReceiver));
    }

}
