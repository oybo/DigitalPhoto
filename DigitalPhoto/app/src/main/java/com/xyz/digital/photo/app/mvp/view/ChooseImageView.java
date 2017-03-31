package com.xyz.digital.photo.app.mvp.view;

import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.mvp.base.BaseView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by O on 2017/3/18.
 */

public interface ChooseImageView extends BaseView {

    void callbackMedias(HashMap<String, List<MediaFileBean>> images);

}
