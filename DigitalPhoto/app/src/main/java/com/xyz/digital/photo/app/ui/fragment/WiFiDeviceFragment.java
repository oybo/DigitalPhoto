package com.xyz.digital.photo.app.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chanven.lib.cptr.PtrClassicFrameLayout;
import com.chanven.lib.cptr.PtrDefaultHandler;
import com.chanven.lib.cptr.PtrFrameLayout;
import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.WifiAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.mvp.wifi.WiFiContract;
import com.xyz.digital.photo.app.mvp.wifi.WiFiPresenter;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.ui.activity.LoginActivity;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.PubUtils;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.LoadingView;
import com.xyz.digital.photo.app.view.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/4/14.
 */

public class WiFiDeviceFragment extends BaseFragment implements WiFiContract.View {

    @Bind(R.id.fragment_device_refresh_layout) PtrClassicFrameLayout fragmentDeviceRefreshLayout;
    @Bind(R.id.fragment_device_recyclerview) RecyclerView fragmentDeviceRecyclerview;
    @Bind(R.id.view_loading) LoadingView mLoadingView;

    private WiFiContract.Presenter mPresenter;
    private WifiManager mWifiManager;

    private WifiAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_device, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initData();
    }

    private void initView() {
        // 禁止加载更多
        fragmentDeviceRefreshLayout.setLoadMoreEnable(false);
        // 下拉重新扫描WiFi热点
        fragmentDeviceRefreshLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mWifiManager.startScan();
            }
        });

        fragmentDeviceRecyclerview.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        fragmentDeviceRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void initWifi() {
        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    private ScanResult mScanResult;

    private void initData() {
        registerBroadcast();
        initWifi();
        mPresenter = new WiFiPresenter(this);
        mAdapter = new WifiAdapter(getActivity(), mWifiManager);
        fragmentDeviceRecyclerview.setAdapter(mAdapter);

        mLoadingView.show();
        fragmentDeviceRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWifiManager.startScan();
            }
        }, 100);

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                // 去连接登录
                mScanResult = mAdapter.getItem(pos);
                if (PubUtils.isConnectTheWifi(mWifiManager.getConnectionInfo(), mScanResult)) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    return;
                }
                showLoading();
                if (!isConnectTheWifi(mScanResult)) {
                    DeviceManager.getInstance().disConnect();
                }
                boolean success = mPresenter.connect(mWifiManager, mScanResult);
                if (!success) {
                    ToastUtil.showToast(getActivity(), AppContext.getInstance().getSString(R.string.connect_faild_txt));
                    mAdapter.notifyDataSetChanged();
                    hideLoading();
                }
            }
        });
    }

    @Override
    public Activity _getActivity() {
        return getActivity();
    }

    @Override
    public void onCallbackDevice(List<ScanResult> wifis) {
        if (wifis != null) {
            mAdapter.clear();
            mAdapter.appendToList(wifis);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragmentDeviceRefreshLayout.refreshComplete();
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
        mLoadingView.hide();
    }

    @Override
    public void setPresenter(WiFiContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {
        mLoadingView.show();
    }

    @Override
    public void hideLoading() {
        mLoadingView.hide();
    }

    private List<ScanResult> getScanResults() {
        List<ScanResult> scanResults = sortByLevel(mWifiManager.getScanResults());

        if (Constants.FILTRATION) {
            List<ScanResult> temp = new ArrayList<>();
            for (ScanResult scanResult : scanResults) {
                if (scanResult.SSID.startsWith(Constants.FILTRATION_NAME)) {
                    temp.add(scanResult);
                }
            }
            scanResults.clear();
            scanResults.addAll(temp);
        }

        return scanResults;
    }

    //将搜索到的wifi根据信号强度从强到弱进行排序
    private List<ScanResult> sortByLevel(List<ScanResult> resultList) {
        java.util.Collections.sort(resultList, new java.util.Comparator() {

            @Override
            public int compare(Object t1, Object t2) {
                return new Integer(((ScanResult) t2).level).compareTo(new Integer(((ScanResult) t1).level));
            }
        });
        return resultList;
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

    /**
     * 广播接收，监听网络
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // wifi已成功扫描到可用wifi。
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> wiFiResult = getScanResults();
                onCallbackDevice(wiFiResult);
                if (wiFiResult.size() == 0) {
                    // 重新扫描
                    mWifiManager.startScan();
                }
            } //系统wifi的状态
            else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        mWifiManager.startScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        break;
                }
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    mAdapter.notifyDataSetChanged();
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    if (mScanResult != null) {
                        if (PubUtils.isConnectTheWifi(mWifiManager.getConnectionInfo(), mScanResult)) {
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                        } else {
                            ToastUtil.showToast(getActivity(), AppContext.getInstance().getSString(R.string.connect_faild_txt));
                        }
                        mAdapter.notifyDataSetChanged();
                        hideLoading();
                        mScanResult = null;
                    }
                }
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
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        getActivity().unregisterReceiver(mReceiver);
    }
}
