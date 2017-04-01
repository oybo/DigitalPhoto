package com.xyz.digital.photo.app.mvp.device;

import java.util.List;

/**
 * Created by O on 2017/3/31.
 */

public class DevicePresenter implements DeviceContract.Presenter {

    private DeviceContract.View mView;
    private DeviceModel mModel;

    public DevicePresenter(DeviceContract.View view) {
        mView = view;
        mModel = new DeviceModel();
        mView.setPresenter(this);
    }

    @Override
    public void scanWifiDevice() {
        mView.showLoading();
        mModel.loadWifiDevice(mView._getActivity(), new DeviceContract.OnScanDeviceListener() {
            @Override
            public void onDevice(List list) {
                mView.hideLoading();
                mView.onCallbackDevice(list);
            }
        });
    }
}
