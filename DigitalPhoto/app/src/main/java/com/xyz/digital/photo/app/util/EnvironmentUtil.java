package com.xyz.digital.photo.app.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class EnvironmentUtil {

    // 缓存根目录
    public static final String MAIN_STORAGE = "ZXF";
    public static final String IMAGE_STORAGE = "image";
    public static final String VIDEO_STORAGE = "video";
    public static final String AUDIO_STORAGE = "audio";

    public static boolean isSdCard(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ToastUtil.showToast(context, "暂无外部存储设备");
            return false;
        }
        return true;
    }

    public static String getImagePath() {
        String path = getMainFilePath() + File.separator + IMAGE_STORAGE;
        mkdirs(path);
        return path;
    }

    public static String getVideoPath() {
        String path = getMainFilePath() + File.separator + VIDEO_STORAGE;
        mkdirs(path);
        return path;
    }

    public static String getAudioPath() {
        String path = getMainFilePath() + File.separator + AUDIO_STORAGE;
        mkdirs(path);
        return path;
    }

    public static String getMainFilePath() {
        String path = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + MAIN_STORAGE;
        mkdirs(path);
        return path;
    }

    private static void mkdirs(String path) {
        File file = new File(path);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

}
