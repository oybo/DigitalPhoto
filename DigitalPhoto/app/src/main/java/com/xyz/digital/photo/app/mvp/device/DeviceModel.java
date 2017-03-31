package com.xyz.digital.photo.app.mvp.device;

import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

import com.actions.actcommunication.AcEventListener;
import com.actions.actcommunication.ActCommunication;
import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.receiver.WifiDirectBroadcastReceiver;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_P2P_SERVICE;
import static com.xyz.digital.photo.app.R.id.info;

/**
 * Created by O on 2017/3/31.
 */

public class DeviceModel {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private IntentFilter mFilter;
    private WifiDirectBroadcastReceiver mReceiver;
    private WifiP2pInfo info;

    private List peers = new ArrayList();

    public DeviceModel() {
        initIntentFilter();
        initReceiver();
    }

    public void loadWifiDevice() {

    }

    private void initIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initReceiver() {
        mManager = (WifiP2pManager) AppContext.getInstance().getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(AppContext.getInstance(), Looper.myLooper(), null);

        /**     设备扫描监听       */
        WifiP2pManager.PeerListListener mPeerListListerner = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peersList) {
                peers.clear();
                peers.addAll(peersList.getDeviceList());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.clear();
                        mAdapter.appendToList(peers);
                        mAdapter.notifyDataSetChanged();
                    }
                });
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

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, mPeerListListerner, mInfoListener);
        registerReceiver(mReceiver, mFilter);

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
