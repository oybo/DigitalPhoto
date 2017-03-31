package com.xyz.digital.photo.app.mvp.device;

import android.os.AsyncTask;

/**
 * Created by O on 2017/3/31.
 */

public class DevicePresenter implements DeviceContract.Presenter {

    private DeviceContract.View mView;
    private DeviceModel mModel;

    public DevicePresenter(DeviceContract.View view) {
        mView = view;
        mModel = new DeviceModel();
    }

    @Override
    public void scanWifiDevice() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mView.showLoading();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                mModel.loadWifiDevice();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mView.hideLoading();
            }
        }.execute();
    }
}
