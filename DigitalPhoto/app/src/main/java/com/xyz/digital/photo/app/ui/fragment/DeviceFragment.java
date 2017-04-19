package com.xyz.digital.photo.app.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.WifiDeviceAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.mvp.device.DeviceContract;
import com.xyz.digital.photo.app.mvp.device.DevicePresenter;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.ui.activity.LoginActivity;
import com.xyz.digital.photo.app.view.LoadingView;
import com.xyz.digital.photo.app.view.SimpleDividerItemDecoration;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/31.
 */

public class DeviceFragment extends BaseFragment implements DeviceContract.View, View.OnClickListener {

    @Bind(R.id.fragment_device_recyclerview) RecyclerView fragmentDeviceRecyclerview;
    @Bind(R.id.fragment_device_scan_loading) ProgressBar mProgressBar;
    @Bind(R.id.view_loading) LoadingView mLoadingView;

    private DeviceContract.Presenter mPresenter;
    private WifiDeviceAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
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
        fragmentDeviceRecyclerview.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        fragmentDeviceRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        getView().findViewById(R.id.fragment_device_resetscan_txt).setOnClickListener(this);
    }

    private void initData() {
        new DevicePresenter(this);
        mAdapter = new WifiDeviceAdapter(getActivity());
        mAdapter.append(new WifiP2pDevice());
        mAdapter.append(new WifiP2pDevice());
        mAdapter.append(new WifiP2pDevice());
        fragmentDeviceRecyclerview.setAdapter(mAdapter);

//        mPresenter.scanWifiDevice();

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                // 去连接登录
//                WifiP2pDevice wifiP2pDevice = mAdapter.getItem(pos);
//                ToastUtil.showToast(getActivity(), wifiP2pDevice.status + "");
//                mPresenter.connect(wifiP2pDevice);

                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });
    }

    @Override
    public Activity _getActivity() {
        return getActivity();
    }

    @Override
    public void onCallbackDevice(List<WifiP2pDevice> wifiP2pDevices) {
        if(wifiP2pDevices != null) {
            mAdapter.clear();
            mAdapter.appendToList(wifiP2pDevices);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void setPresenter(DeviceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_device_resetscan_txt:
                mPresenter.scanWifiDevice();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unRegisterReceiver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
