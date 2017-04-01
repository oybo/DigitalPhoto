package com.xyz.digital.photo.app.mvp.Photo;

import android.app.Activity;

import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.mvp.BasePresenter;
import com.xyz.digital.photo.app.mvp.BaseView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by O on 2017/4/1.
 */

public interface PhotoContract {

    enum MEDIA_SHOW_TYPE {
        CHART, LIST
    }

    enum MEDIA_FILE_TYPE {
        IMAGE, AUDIO, VIDEO
    }

    interface View extends BaseView<Presenter> {
        Activity _getActivity();

        void onCallbackMediasByList(HashMap<String, List<MediaFileBean>> files);

        void onCallbackMediasByChart(List<MediaFileBean> files);
    }

    interface Presenter extends BasePresenter {
        void showType(MEDIA_SHOW_TYPE type);

        void showMediaFiles(PhotoContract.MEDIA_FILE_TYPE type);

        List<FolderBean> subGroupOfMedia(HashMap<String, List<MediaFileBean>> images);
    }

}
