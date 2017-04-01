package com.xyz.digital.photo.app.mvp.device;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

import com.xyz.digital.photo.app.receiver.WifiDirectBroadcastReceiver;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_P2P_SERVICE;

/**
 * Created by O on 2017/3/31.
 */

public class DeviceModel {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private IntentFilter mFilter;
    private WifiDirectBroadcastReceiver mReceiver;
    private WifiP2pInfo info;

    private List<WifiP2pDevice> mWifiP2pDevices = new ArrayList();

    public DeviceModel() {
        initIntentFilter();
    }

    public void loadWifiDevice(Activity activity, DeviceContract.OnScanDeviceListener onScanDeviceListener) {
        initReceiver(activity, onScanDeviceListener);
    }

    private void initIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initReceiver(final Activity activity, final DeviceContract.OnScanDeviceListener onScanDeviceListener) {
        mManager = (WifiP2pManager) activity.getApplicationContext().getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(activity.getApplicationContext(), Looper.myLooper(), null);

        /**     设备扫描监听       */
        WifiP2pManager.PeerListListener mPeerListListerner = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peersList) {
                try {
                    mWifiP2pDevices.clear();
                    mWifiP2pDevices.addAll(peersList.getDeviceList());

                    if(null != activity) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onScanDeviceListener.onDevice(mWifiP2pDevices);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        /**     设备连接监听       */
        WifiP2pManager.ConnectionInfoListener mInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo minfo) {
                // 这里可以查看变化后的网络信息

                // 通过传递进来的WifiP2pInfo参数获取变化后的地址信息
                InetAddress groupOwnerAddress = info.groupOwnerAddress;
                // 通过协商，决定一个小组的组长
                if (info.groupFormed && info.isGroupOwner) {
                    // 这里执行P2P小组组长的任务。 通常是创建一个服务线程来监听客户端的请求
                } else if (info.groupFormed) {
                    // 这里执行普通组员的任务 通常是创建一个客户端向组长的服务器发送请求
                }

            }
        };

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, activity, mPeerListListerner, mInfoListener);
        activity.registerReceiver(mReceiver, mFilter);

        // 初始化peers并开始搜索
        discoverPeers();
    }

    /**
     * 初始化peers
     */
    private void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

}
