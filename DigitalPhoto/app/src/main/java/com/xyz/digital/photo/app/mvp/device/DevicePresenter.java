package com.xyz.digital.photo.app.mvp.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.util.ToastUtil;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_P2P_SERVICE;

/**
 * Created by O on 2017/3/31.
 */

public class DevicePresenter implements DeviceContract.Presenter {

    private DeviceContract.View mView;

    private IntentFilter mIntentFilter;
    private List<WifiP2pDevice> mWifiP2pDevices = new ArrayList();

    private WifiManager MWifiManager;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;

    public DevicePresenter(DeviceContract.View view) {
        mView = view;
        mView.setPresenter(this);

        mWifiP2pManager = (WifiP2pManager) view._getActivity().getApplicationContext().getSystemService(WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(view._getActivity(), view._getActivity().getMainLooper(), null);
        // 注册广播接收器
        registerReceiver();
    }

    @Override
    public void scanWifiDevice() {
        // 启动扫描
        discoverDevice();
    }

    private void discoverDevice() {
        if (mWifiP2pManager != null) {
            //正在搜索，设置控件的显示状态。
            mView.showLoading();
            mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    //搜索成功
                }

                public void onFailure(int reason) {
                    //搜索失败
                }
            });
        }
    }

    @Override
    public void registerReceiver() {
        // wifi p2p
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mView._getActivity().registerReceiver(mP2PReceiver, mIntentFilter);
        // wifi hw changed
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mView._getActivity().registerReceiver(mWifiHWReceiver, mIntentFilter);
    }

    @Override
    public void unRegisterReceiver() {
        mView._getActivity().unregisterReceiver(mP2PReceiver);
    }

    @Override
    public void connect(WifiP2pDevice wifiP2pDevice) {
        // 设备连接
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiP2pDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // success
            }
            @Override
            public void onFailure(int reason) {
                ToastUtil.showToast(mView._getActivity(), AppContext.getInstance().getSString(R.string.connect_faild_txt));
            }
        });
    }

    /**
     * 设备扫描监听
     */
    WifiP2pManager.PeerListListener mPeerListListerner = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peersList) {
            mView.hideLoading();
            try {
                mWifiP2pDevices.clear();
                mWifiP2pDevices.addAll(peersList.getDeviceList());
                mView.onCallbackDevice(mWifiP2pDevices);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 设备连接监听
     */
    WifiP2pManager.ConnectionInfoListener mInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo minfo) {
            // 这里可以查看变化后的网络信息
            // 通过传递进来的WifiP2pInfo参数获取变化后的地址信息
            InetAddress groupOwnerAddress = minfo.groupOwnerAddress;
            // 通过协商，决定一个小组的组长
            if (minfo.groupFormed && minfo.isGroupOwner) {
                // 这里执行P2P小组组长的任务。 通常是创建一个服务线程来监听客户端的请求
            } else if (minfo.groupFormed) {
                // 这里执行普通组员的任务 通常是创建一个客户端向组长的服务器发送请求
            }

        }
    };

    private final BroadcastReceiver mP2PReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                //从WIFI_P2P_STATE_CHANGED_ACTION广播中获取相关状态信息，判断是否能够打开WiFi Direct
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // WiFi P2P 可以使用
                } else {
                    // WiFi P2P 不可以使用
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // 去获取设备列表
                if (mWifiP2pManager != null) {
                    //获取P2P Device信息列表。
                    mWifiP2pManager.requestPeers(mChannel, mPeerListListerner);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if (mWifiP2pManager != null) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if (networkInfo.isConnected()) {
                        mWifiP2pManager.requestConnectionInfo(mChannel, mInfoListener);
                    } else {
                    }
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // 更新本设备的信息
                WifiP2pDevice mThisDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            }
        }
    };

    private BroadcastReceiver mWifiHWReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            if (MWifiManager.WIFI_DEVICE_REMOVED_ACTION.equals(action)) {
//                //WiFi被移除
//                onWifiHWChanged(false);
//            } else if (MWifiManager.WIFI_DEVICE_ADDED_ACTION.equals(action)) {
//                //有WiFi
//                onWifiHWChanged(true);
//            }
        }
    };

}
