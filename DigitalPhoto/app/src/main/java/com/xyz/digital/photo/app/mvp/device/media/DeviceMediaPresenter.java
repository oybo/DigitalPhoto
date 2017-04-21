package com.xyz.digital.photo.app.mvp.device.media;

import android.os.AsyncTask;
import android.os.Environment;

import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;
import com.xyz.digital.photo.app.mvp.Photo.MultiMediaUtils;
import com.xyz.digital.photo.app.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by O on 2017/4/1.
 */

public class DeviceMediaPresenter implements DeviceMediaContract.Presenter {

    private DeviceMediaContract.View mView;
    private DeviceMediaModel mModel;

    private MEDIA_SHOW_TYPE mShowType;

    public DeviceMediaPresenter(DeviceMediaContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void showType(MEDIA_SHOW_TYPE type) {
        mShowType = type;
        loadMediaFiles(true, MEDIA_FILE_TYPE.IMAGE);
    }

    @Override
    public void showMediaFiles(final MEDIA_FILE_TYPE type) {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ToastUtil.showToast(mView._getActivity(), "暂无外部存储设备");
            return;
        }

        loadMediaFiles(false, type);
    }

    private void loadMediaFiles(final boolean isRefreshModel, final MEDIA_FILE_TYPE type) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mView.showLoading();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                HashMap<String, List<MediaFileBean>> mGruopMap = new HashMap<>();

                if(type == MEDIA_FILE_TYPE.IMAGE) {
                    // 图片
                    MultiMediaUtils.getAllImages(mView._getActivity(), mGruopMap);
                } else if(type == MEDIA_FILE_TYPE.AUDIO) {
                    // 音乐
                    MultiMediaUtils.getAllAudios(mView._getActivity(), mGruopMap);
                } else if(type == MEDIA_FILE_TYPE.VIDEO) {
                    // 视频
                    MultiMediaUtils.getAllVideos(mView._getActivity(), mGruopMap);
                } else if(type == MEDIA_FILE_TYPE.ALL) {
                    // 全部
                }

                if(mShowType == MEDIA_SHOW_TYPE.LIST) {
                    // 列表模式
                    mView.onCallbackMediasByList(isRefreshModel, mGruopMap);
                } else {
                    // 图表模式
                    List<MediaFileBean> files = new ArrayList<MediaFileBean>();
                    for(Map.Entry<String, List<MediaFileBean>> entry :  mGruopMap.entrySet()) {
                        files.addAll(entry.getValue());
                    }
                    mView.onCallbackMediasByChart(isRefreshModel, files);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                mView.hideLoading();
            }
        }.execute();
    }

    @Override
    public List<FolderBean> subGroupOfMedia(HashMap<String, List<MediaFileBean>> images) {
        return MultiMediaUtils.subGroupOfMedia(images);
    }

}
