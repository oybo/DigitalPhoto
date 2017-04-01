package com.xyz.digital.photo.app.mvp.Photo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by O on 2017/3/21.
 */

public class MultiMediaUtils {

    /**
     * 获取sd卡所有的图片文件
     * @param context
     * @return
     */
    public static void getAllImages(Context context, HashMap<String, List<MediaFileBean>> mGruopMap) {
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
                //获取路径
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                //获取名称
                String fileName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.TITLE));
                //获取父路径名
                String parentName = new File(path).getParentFile().getName();

                MediaFileBean mediaFileBean = new MediaFileBean();
                mediaFileBean.setFilePath(path);
                mediaFileBean.setFileName(fileName);
                mediaFileBean.setFileType(PhotoContract.MEDIA_FILE_TYPE.IMAGE);

                //根据父路径名将图片放入到mGruopMap中
                if (!mGruopMap.containsKey(parentName)) {
                    List<MediaFileBean> chileList = new ArrayList<MediaFileBean>();
                    mGruopMap.put(parentName, chileList);
                }

                mGruopMap.get(parentName).add(mediaFileBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(mCursor != null) {
                mCursor.close();
            }
        }
    }

    /**
     * 获取sd卡所有的音乐文件
     *
     * @return
     * @throws Exception
     */
    public static void getAllAudios(Context context, HashMap<String, List<MediaFileBean>> mGruopMap) {

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                    MediaStore.Audio.Media.MIME_TYPE + "=? or "
                            + MediaStore.Audio.Media.MIME_TYPE + "=?",
                    new String[]{"audio/mpeg", "audio/x-ms-wma"}, MediaStore.Audio.Media.DATE_MODIFIED);

            while (cursor.moveToNext()) {
                //获取路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                //获取名称
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                //获取父路径名
                String parentName = new File(path).getParentFile().getName();

                MediaFileBean mediaFileBean = new MediaFileBean();
                mediaFileBean.setFilePath(path);
                mediaFileBean.setFileName(fileName);
                mediaFileBean.setFileType(PhotoContract.MEDIA_FILE_TYPE.AUDIO);

                //根据父路径名将图片放入到mGruopMap中
                if (!mGruopMap.containsKey(parentName)) {
                    List<MediaFileBean> chileList = new ArrayList<MediaFileBean>();
                    mGruopMap.put(parentName, chileList);
                }

                mGruopMap.get(parentName).add(mediaFileBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 获取sd卡所有的视频文件
     *
     * @return
     * @throws Exception
     */
    public static void getAllVideos(Context context, HashMap<String, List<MediaFileBean>> mGruopMap) {

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Video.Media._ID,
                            MediaStore.Video.Media.DATA,
                            MediaStore.Video.Media.TITLE,
                            MediaStore.Video.Media.MIME_TYPE
                    },
                    null, null, null);

            while (cursor.moveToNext()) {
                //获取路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                //获取名称
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                //获取父路径名
                String parentName = new File(path).getParentFile().getName();

                MediaFileBean mediaFileBean = new MediaFileBean();
                mediaFileBean.setFilePath(path);
                mediaFileBean.setFileName(fileName);
                mediaFileBean.setFileType(PhotoContract.MEDIA_FILE_TYPE.VIDEO);

                //根据父路径名将图片放入到mGruopMap中
                if (!mGruopMap.containsKey(parentName)) {
                    List<MediaFileBean> chileList = new ArrayList<MediaFileBean>();
                    mGruopMap.put(parentName, chileList);
                }

                mGruopMap.get(parentName).add(mediaFileBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中
     * 所以需要遍历HashMap将数据组装成List
     *
     * @param mGruopMap
     * @return
     */
    public static List<FolderBean> subGroupOfMedia(HashMap<String, List<MediaFileBean>> mGruopMap){
        if(mGruopMap.size() == 0){
            return null;
        }
        List<FolderBean> list = new ArrayList<FolderBean>();

        Iterator<Map.Entry<String, List<MediaFileBean>>> it = mGruopMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<MediaFileBean>> entry = it.next();
            FolderBean mImageBean = new FolderBean();
            String key = entry.getKey();
            List<MediaFileBean> value = entry.getValue();

            mImageBean.setFolderName(key);
            mImageBean.setImageCounts(value.size());
            mImageBean.setTopImagePath(value.get(0).getFilePath());//获取该组的第一张图片
            mImageBean.setFileType(value.get(0).getFileType());

            list.add(mImageBean);
        }

        return list;
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     * @param videoPath 视频的路径
     * @param width 指定输出视频缩略图的宽度
     * @param height 指定输出视频缩略图的高度度
     * @param kind 参照MediaStore.Images(Video).Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        if(bitmap!= null){
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

}
