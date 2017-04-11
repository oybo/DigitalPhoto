package com.xyz.digital.photo.app.manager;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xyz.digital.photo.app.R;

/**
 * 图片加载类
 * Created by O on 2017/2/24.
 *  禁止内存缓存：
    .skipMemoryCache(true)
    清除内存缓存：
    １) 必须在UI线程中调用 Glide.get(context).clearMemory();
    禁止磁盘缓存：
    .diskCacheStrategy(DiskCacheStrategy.NONE)
    必须在后台线程中调用，建议同时clearMemory()
    Glide.get(applicationContext).clearDiskCache();
    优先加载
     .priority(Priority.HIGH)
    后加载
    .priority(Priority.LOW)
 *
 */

public class ImageLoadManager {

    public static void setImage(String url, final ImageView imageView) {

        setImage(url, imageView, R.drawable.defult_audio_icon);

    }

    public static void setImage(String url, final ImageView imageView, int errorId) {

        try {
            Glide.with(imageView.getContext())
                    .load(url)
                    .error(errorId)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
