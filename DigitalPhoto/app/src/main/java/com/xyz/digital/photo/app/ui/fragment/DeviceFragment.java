package com.xyz.digital.photo.app.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.WifiDeviceAdapter;
import com.xyz.digital.photo.app.mvp.device.DeviceContract;
import com.xyz.digital.photo.app.mvp.device.DevicePresenter;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/31.
 */

public class DeviceFragment extends Fragment implements Toolbar.OnMenuItemClickListener, DeviceContract.View {

    @Bind(R.id.fragment_device_toolbar) Toolbar fragmentDeviceToolbar;
    @Bind(R.id.fragment_device_recyclerview) RecyclerView fragmentDeviceRecyclerview;

    private DeviceContract.Presenter mPresenter;

    private WifiDeviceAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new WifiDeviceAdapter(getActivity());
    }

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
        fragmentDeviceToolbar.setTitle("");
        fragmentDeviceToolbar.inflateMenu(R.menu.menu_device_view);
        fragmentDeviceToolbar.setOnMenuItemClickListener(this);
    }

    private void initData() {
        new DevicePresenter(this);
        mPresenter.scanWifiDevice();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.menu_device_telnet) {
            // 远程登录

        }
        return false;
    }

    @Override
    public Context _getContext() {
        return getActivity();
    }

    @Override
    public void setPresenter(DeviceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
