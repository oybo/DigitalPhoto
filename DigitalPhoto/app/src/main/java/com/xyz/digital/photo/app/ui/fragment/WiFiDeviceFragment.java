package com.xyz.digital.photo.app.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.ScanResult;
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
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.WifAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.mvp.wifi.WiFiContract;
import com.xyz.digital.photo.app.mvp.wifi.WiFiPresenter;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.ui.activity.LoginActivity;
import com.xyz.digital.photo.app.view.LoadingView;
import com.xyz.digital.photo.app.view.SimpleDividerItemDecoration;

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
        // 禁止加载更多
        fragmentDeviceRefreshLayout.setLoadMoreEnable(false);
        // 下拉重新扫描WiFi热点
        fragmentDeviceRefreshLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mPresenter.scanWiFi();
            }
        });

        fragmentDeviceRecyclerview.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        fragmentDeviceRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void initData() {
        mPresenter = new WiFiPresenter(this);
        mAdapter = new WifAdapter(getActivity());
        fragmentDeviceRecyclerview.setAdapter(mAdapter);

        mLoadingView.show();
        fragmentDeviceRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.scanWiFi();
            }
        }, 100);

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                // 去连接登录
                ScanResult scanResult = mAdapter.getItem(pos);
                mPresenter.connect(scanResult);
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public Activity _getActivity() {
        return getActivity();
    }

    @Override
    public void onCallbackConnect() {
        startActivity(new Intent(getActivity(), LoginActivity.class));
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
    public void notifyDataSetChanged() {
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.unRegisterBroadcast();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
