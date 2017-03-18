package com.xyz.digital.photo.app.mvp.presenter;

import android.os.AsyncTask;
import android.os.Environment;

import com.xyz.digital.photo.app.bean.ImageBean;
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

    public void getImages() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ToastUtil.showToast(mView._getActivity(), "暂无外部存储");
            return;
        }
        new AsyncTask<Void, Void, HashMap<String, List<String>>>() {
            @Override
            protected HashMap<String, List<String>> doInBackground(Void... voids) {
                return mModel.getImages(mView._getActivity());
            }

            @Override
            protected void onPostExecute(HashMap<String, List<String>> result) {
                super.onPostExecute(result);
                mView.callbackImages(result);
            }
        }.execute();

    }

    public List<ImageBean> subGroupOfImage(HashMap<String, List<String>> images) {
        return mModel.subGroupOfImage(images);
    }

}
