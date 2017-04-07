package com.xyz.digital.photo.app.mvp.Photo;

import android.os.AsyncTask;
import android.os.Environment;

import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by O on 2017/4/1.
 */

public class PhotoPresenter implements PhotoContract.Presenter {

    private PhotoContract.View mView;

    private PhotoContract.MEDIA_SHOW_TYPE mShowType;

    public PhotoPresenter(PhotoContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void showType(PhotoContract.MEDIA_SHOW_TYPE type) {
        mShowType = type;
        showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.IMAGE);
    }

    @Override
    public void showMediaFiles(final PhotoContract.MEDIA_FILE_TYPE type) {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ToastUtil.showToast(mView._getActivity(), "暂无外部存储");
            return;
        }

        new AsyncTask<Void, Void, HashMap<String, List<MediaFileBean>>>() {
            @Override
            protected HashMap<String, List<MediaFileBean>> doInBackground(Void... voids) {
                HashMap<String, List<MediaFileBean>> mGruopMap = new HashMap<>();

                if(type == PhotoContract.MEDIA_FILE_TYPE.IMAGE) {
                    // 图片
                    MultiMediaUtils.getAllImages(mView._getActivity(), mGruopMap);
                } else if(type == PhotoContract.MEDIA_FILE_TYPE.AUDIO) {
                    // 音乐
                    MultiMediaUtils.getAllAudios(mView._getActivity(), mGruopMap);
                } else {
                    // 视频
                    MultiMediaUtils.getAllVideos(mView._getActivity(), mGruopMap);
                }

                return mGruopMap;
            }

            @Override
            protected void onPostExecute(HashMap<String, List<MediaFileBean>> mGruopMap) {
                super.onPostExecute(mGruopMap);
                if(mShowType == PhotoContract.MEDIA_SHOW_TYPE.LIST) {
                    // 列表模式
                    mView.onCallbackMediasByList(mGruopMap);
                } else {
                    // 图表模式
                    List<MediaFileBean> files = new ArrayList<MediaFileBean>();
                    for(Map.Entry<String, List<MediaFileBean>> entry :  mGruopMap.entrySet()) {
                        files.addAll(entry.getValue());
                    }
                    mView.onCallbackMediasByChart(files);
                }
            }
        }.execute();

    }

    @Override
    public List<FolderBean> subGroupOfMedia(HashMap<String, List<MediaFileBean>> images) {
        return MultiMediaUtils.subGroupOfMedia(images);
    }

}
