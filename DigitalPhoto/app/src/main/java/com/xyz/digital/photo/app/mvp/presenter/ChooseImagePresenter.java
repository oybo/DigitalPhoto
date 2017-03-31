package com.xyz.digital.photo.app.mvp.presenter;

import android.os.AsyncTask;
import android.os.Environment;

import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.mvp.model.ChooseImageModel;
import com.xyz.digital.photo.app.mvp.view.ChooseImageView;
import com.xyz.digital.photo.app.util.ToastUtil;

import java.util.HashMap;
import java.util.List;

/**
 * Created by O on 2017/3/18.
 */

public class ChooseImagePresenter {

    private ChooseImageModel mModel;
    private ChooseImageView mView;

    public ChooseImagePresenter(ChooseImageView view) {
        mView = view;
        mModel = new ChooseImageModel();
    }

    public void loadMediaFiles() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ToastUtil.showToast(mView._getActivity(), "暂无外部存储");
            return;
        }
        new AsyncTask<Void, Void, HashMap<String, List<MediaFileBean>>>() {
            @Override
            protected HashMap<String, List<MediaFileBean>> doInBackground(Void... voids) {
                return mModel.getAllMediaFiles(mView._getActivity());
            }

            @Override
            protected void onPostExecute(HashMap<String, List<MediaFileBean>> result) {
                super.onPostExecute(result);
                mView.callbackMedias(result);
            }
        }.execute();
    }

    public List<FolderBean> subGroupOfMedia(HashMap<String, List<MediaFileBean>> images) {
        return mModel.subGroupOfMedia(images);
    }

}
