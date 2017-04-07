package com.xyz.digital.photo.app.ui.activity;

import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.WifiDeviceAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.receiver.WifiDirectBroadcastReceiver;
import com.xyz.digital.photo.app.ui.BaseActivity;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/20.
 */

public class ConnectWifiActivity extends BaseActivity {

    @Bind(R.id.activity_wifi_device_recyclerview) RecyclerView mRecyclerView;

    private WifiDirectBroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pInfo info;

    private List peers = new ArrayList();
    private WifiDeviceAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_wifi);
        ButterKnife.bind(this);

        initView();
        initIntentFilter();
        initReceiver();
    }

    private void initView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new WifiDeviceAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                connect(pos);
            }
        });
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
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getApplicationContext(), Looper.myLooper(), null);

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

                String ss = "";

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

    private void connect(int pos) {
//        final WifiP2pDevice device = mAdapter.getItem(pos); //从peers列表中获取发现来的第一个设备
//        WifiP2pConfig config = new WifiP2pConfig();
//        config.deviceAddress = device.deviceAddress;
//        config.wps.setup = WpsInfo.PBC;
//
//        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                // 连接成功
//                Toast.makeText(getApplicationContext(), "与设备" + device.deviceName + "连接成功", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onFailure(int arg0) {
//                // 连接失败
//                Toast.makeText(getApplicationContext(), "与设备" + device.deviceName + "连接失败", Toast.LENGTH_LONG).show();
//            }
//        });
    }

    /**    停止扫描      */
    private void stopConnect() {
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        stopConnect();
    }

    @Override
    protected int getToolBarMenuView() {
        return 0;
    }

    @Override
    protected void onItemMenuSelected(MenuItem item) {

    }
}
