package com.xyz.digital.photo.app.manager;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.util.PubUtils;


public class ImageLoadManager {

    public static void setImage(String url, final ImageView imageView) {
        int errorImage = R.drawable.defult_image_icon;
        MEDIA_FILE_TYPE type = PubUtils.getFileType(url);
        if(type == MEDIA_FILE_TYPE.AUDIO) {
            errorImage = R.drawable.defult_audio_icon;
        } else if(type == MEDIA_FILE_TYPE.VIDEO) {
            errorImage = R.drawable.defult_video_icon;
        }

        setImage(url, imageView, errorImage);
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
