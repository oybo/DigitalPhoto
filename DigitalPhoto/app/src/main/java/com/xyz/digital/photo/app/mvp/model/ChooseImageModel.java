package com.xyz.digital.photo.app.mvp.model;

import android.content.Context;

import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.manager.MultiMediaUtils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by O on 2017/3/18.
 */

public class ChooseImageModel {

    public ChooseImageModel() {

    }

    public HashMap<String, List<MediaFileBean>> getAllMediaFiles(Context context) {
        return MultiMediaUtils.getAllMediaFiles(context);
    }

    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中
     * 所以需要遍历HashMap将数据组装成List
     *
     * @param mGruopMap
     * @return
     */
    public List<FolderBean> subGroupOfMedia(HashMap<String, List<MediaFileBean>> mGruopMap){
        return MultiMediaUtils.subGroupOfMedia(mGruopMap);
    }

}
