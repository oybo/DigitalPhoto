package com.xyz.digital.photo.app.mvp.device;

import android.content.Context;
import com.xyz.digital.photo.app.mvp.BasePresenter;
import com.xyz.digital.photo.app.mvp.BaseView;

/**
 * Created by O on 2017/3/31.
 */

public interface DeviceContract {

    interface View extends BaseView<Presenter> {
        Context _getContext();
    }

    interface Presenter extends BasePresenter {
        void scanWifiDevice();
    }

}
