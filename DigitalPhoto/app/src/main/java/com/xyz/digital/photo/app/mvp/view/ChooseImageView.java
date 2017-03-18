package com.xyz.digital.photo.app.mvp.view;

import com.xyz.digital.photo.app.mvp.base.BaseView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by O on 2017/3/18.
 */

public interface ChooseImageView extends BaseView {

    void callbackImages(HashMap<String, List<String>> images);

}
