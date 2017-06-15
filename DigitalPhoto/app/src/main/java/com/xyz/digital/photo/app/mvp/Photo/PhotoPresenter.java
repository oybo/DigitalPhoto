package com.xyz.digital.photo.app.mvp.Photo;

import android.os.AsyncTask;
import android.os.Environment;

import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE.IMAGE;

/**
 * Created by O on 2017/4/1.
 */

public class PhotoPresenter implements PhotoContract.Presenter {

    private PhotoContract.View mView;

    private MEDIA_SHOW_TYPE mShowType;

    private MEDIA_FILE_TYPE mShowFileType;

    public PhotoPresenter(PhotoContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void showType(MEDIA_SHOW_TYPE type) {
        mShowType = type;
        if(mShowFileType == null) {
            mShowFileType = MEDIA_FILE_TYPE.IMAGE;
        }
        loadMediaFiles(true, mShowFileType);
    }

    @Override
    public void showMediaFiles(final MEDIA_FILE_TYPE type) {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        loadMediaFiles(false, type);
    }

    private void loadMediaFiles(final boolean isRefreshModel, final MEDIA_FILE_TYPE type) {
        mShowFileType = type;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mView.showLoading();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                HashMap<String, List<MediaFileBean>> mGruopMap = new HashMap<>();

                if(type == IMAGE) {
                    // 图片
                    MultiMediaUtils.getAllImages(mView._getActivity(), mGruopMap);
                } else if(type == MEDIA_FILE_TYPE.AUDIO) {
                    // 音乐
                    MultiMediaUtils.getAllAudios(mView._getActivity(), mGruopMap);
                } else {
                    // 视频
                    MultiMediaUtils.getAllVideos(mView._getActivity(), mGruopMap);
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
                    int len = files.size();
                    for (int i = 0; i < len; i++) {
                        MediaFileBean mediaFileBean = files.get(i);
                        mediaFileBean.setPosition(i);
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
