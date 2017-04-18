package com.xyz.digital.photo.app.ui.fragment;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.WifAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.mvp.wifi.WiFiContract;
import com.xyz.digital.photo.app.mvp.wifi.WiFiPresenter;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.view.LoadingView;
import com.xyz.digital.photo.app.view.SimpleDividerItemDecoration;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/4/14.
 */

public class WiFiDeviceFragment extends BaseFragment implements WiFiContract.View, View.OnClickListener {

    @Bind(R.id.fragment_device_recyclerview) RecyclerView fragmentDeviceRecyclerview;
    @Bind(R.id.fragment_device_scan_loading) ProgressBar mProgressBar;
    @Bind(R.id.view_loading) LoadingView mLoadingView;

    private WiFiContract.Presenter mPresenter;

    private WifAdapter mAdapter;

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
        fragmentDeviceRecyclerview.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        fragmentDeviceRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        getView().findViewById(R.id.fragment_device_resetscan_txt).setOnClickListener(this);
    }

    private void initData() {
        mPresenter = new WiFiPresenter(this);
        mAdapter = new WifAdapter(getActivity());
        fragmentDeviceRecyclerview.setAdapter(mAdapter);

        mPresenter.scanWiFi();

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                // 去连接登录
                ScanResult scanResult = mAdapter.getItem(pos);
                mPresenter.connect(scanResult);

//                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });
    }

    @Override
    public Activity _getActivity() {
        return getActivity();
    }

    @Override
    public void onCallbackDevice(List<ScanResult> wifis) {
        if(wifis != null) {
            mAdapter.clear();
            mAdapter.appendToList(wifis);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void setPresenter(WiFiContract.Presenter presenter) {
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
                mPresenter.scanWiFi();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.unRegisterBroadcast();
    }
}
