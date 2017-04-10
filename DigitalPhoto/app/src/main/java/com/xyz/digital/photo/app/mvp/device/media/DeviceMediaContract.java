package com.xyz.digital.photo.app.mvp.device.media;

import android.app.Activity;

import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;
import com.xyz.digital.photo.app.mvp.BasePresenter;
import com.xyz.digital.photo.app.mvp.BaseView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by O on 2017/4/1.
 */

public interface DeviceMediaContract {

    interface View extends BaseView<Presenter> {
        Activity _getActivity();

        void onCallbackMediasByList(boolean isRefreshModel, HashMap<String, List<MediaFileBean>> files);

        void onCallbackMediasByChart(boolean isRefreshModel, List<MediaFileBean> files);
    }

    interface Presenter extends BasePresenter {
        void showType(MEDIA_SHOW_TYPE type);

        void showMediaFiles(MEDIA_FILE_TYPE type);

        List<FolderBean> subGroupOfMedia(HashMap<String, List<MediaFileBean>> images);
    }

}
