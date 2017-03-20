package com.xyz.digital.photo.app.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.xyz.digital.photo.app.bean.ImageBean;
import com.xyz.digital.photo.app.bean.SongBean;
import com.xyz.digital.photo.app.bean.VideoBean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by O on 2017/3/20.
 */

public class ImageUtils {

    /**
     * 获取sd卡所有的图片文件
     * @param context
     * @return
     */
    public static HashMap<String, List<String>> getAllImages(Context context) {
        HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();

        Cursor mCursor = null;
        try {
            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = context.getContentResolver();

            //只查询jpeg和png的图片
            mCursor = mContentResolver.query(mImageUri, null,
                    MediaStore.Images.Media.MIME_TYPE + "=? or "
                            + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

            while (mCursor.moveToNext()) {
                //获取图片的路径
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                //获取该图片的父路径名
                String parentName = new File(path).getParentFile().getName();

                //根据父路径名将图片放入到mGruopMap中
                if (!mGruopMap.containsKey(parentName)) {
                    List<String> chileList = new ArrayList<String>();
                    chileList.add(path);
                    mGruopMap.put(parentName, chileList);
                } else {
                    mGruopMap.get(parentName).add(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(mCursor != null) {
                mCursor.close();
            }
        }
        ArrayList<VideoBean> videos = VideoUtils.getAllVideo(context);
        ArrayList<SongBean> songs = AudioUtils.getAllSongs(context);
        return mGruopMap;
    }

    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中
     * 所以需要遍历HashMap将数据组装成List
     *
     * @param mGruopMap
     * @return
     */
    public static List<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGruopMap){
        if(mGruopMap.size() == 0){
            return null;
        }
        List<ImageBean> list = new ArrayList<ImageBean>();

        Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            ImageBean mImageBean = new ImageBean();
            String key = entry.getKey();
            List<String> value = entry.getValue();

            mImageBean.setFolderName(key);
            mImageBean.setImageCounts(value.size());
            mImageBean.setTopImagePath(value.get(0));//获取该组的第一张图片

            list.add(mImageBean);
        }

        return list;
    }

}
