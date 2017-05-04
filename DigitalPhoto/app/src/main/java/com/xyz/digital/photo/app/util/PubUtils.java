package com.xyz.digital.photo.app.util;

import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;

import java.io.File;
import java.math.BigDecimal;

import static com.xyz.digital.photo.app.manager.DeviceManager.mRemoteCurrentPath;

/**
 * Created by O on 2017/4/5.
 */

public class PubUtils {

    /**
     * 计算文件夹的总大小
     *
     * @param file
     * @return
     */
    public static long getDirSize(File file) {
        long size = 0;
        // 判断文件是否存在
        try {
            if (file.exists()) {
                // 如果是目录则递归计算其内容的总大小
                if (file.isDirectory()) {
                    File[] children = file.listFiles();
                    for (File f : children)
                        size += getDirSize(f);
                    return size;
                } else {// 如果是文件则直接返回其大小,以“兆”为单位
                    size = file.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static long conversionSize(String sizeStr) {
        long size = 0;
        if (sizeStr.contains("G")) {
            sizeStr = sizeStr.replace("G", "").replace(" ", "");
            size = Integer.parseInt(sizeStr);
            size = size * 1024 * 1024 * 1024;
        } else if (sizeStr.contains("M")) {
            sizeStr = sizeStr.replace("M", "").replace(" ", "").replace("B", "");
            size = Integer.parseInt(sizeStr);
            size = size * 1024 * 1024;
        } else if (sizeStr.contains("K")) {
            sizeStr = sizeStr.replace("K", "").replace(" ", "").replace("B", "");
            size = Integer.parseInt(sizeStr);
            size = size * 1024;
        } else {
            size = Integer.parseInt(sizeStr);
            size = size * 1024;
        }
        return size;
    }

    /**
     * 格式化文件大小
     *
     * @param size
     * @return
     */
    public static String formatFileLen(long size) {
        if (size >= 1024 * 1024 * 1024) {
            return String.format("%.1fG", (size * 1.0) / (1024 * 1024 * 1024));
        }
        if (size > 1024 * 1024) {
            return String.format("%.1fM", (size * 1.0) / (1024 * 1024));
        }
        if (size > 1024) {
            return String.format("%.1fK", (size * 1.0) / 1024);
        }
        return size + "B";
    }

    // 递归方式 计算文件的大小
    public static long getTotalSizeOfFilesInDir(final File file) {
        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children)
                total += getTotalSizeOfFilesInDir(child);
        return total;
    }

    /**
     * 计算百分比
     *
     * @param all , pro
     * @return
     */
    public static String getSHCollagen(int all, int pro) {
        String str = "0";
        if (all < 0 || pro < 0 || all < pro) {
            return str;
        }
        try {
            double proTemp = (double) pro * 100;
            double allTemp = all;
            BigDecimal bigPro = new BigDecimal(proTemp + "");
            BigDecimal bigAll = new BigDecimal(allTemp + "");
            BigDecimal proDou = bigPro.divide(bigAll, 2, BigDecimal.ROUND_HALF_UP);
            str = proDou.toString();
            if (str.indexOf(".") > 0) {
                str = str.substring(0, str.indexOf("."));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getDonwloadLocalPath(String fileName, MEDIA_FILE_TYPE type) {
        String localPath = "";
        try {
            if (type == MEDIA_FILE_TYPE.VIDEO) {
                localPath = EnvironmentUtil.getVideoPath();
            } else if (type == MEDIA_FILE_TYPE.AUDIO) {
                localPath = EnvironmentUtil.getAudioPath();
            } else {
                localPath = EnvironmentUtil.getImagePath();
            }
            localPath = localPath + mRemoteCurrentPath + fileName;
            File file = new File(localPath);
            File parentFile = new File(file.getParent());
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localPath;
    }

    public static boolean isTypeFile(String fileName, MEDIA_FILE_TYPE type) {
        if(type == MEDIA_FILE_TYPE.ALL) {
            return true;
        }
        // 获取文件后缀名并转化为写，用于后续比较
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        // 创建类型数组
        String[] img = null;
        if (type == MEDIA_FILE_TYPE.IMAGE) {
            img = new String[]{"bmp", "jpg", "jpeg", "png", "tiff", "gif", "pcx", "tga", "exif", "fpx", "svg", "psd",
                    "cdr", "pcd", "dxf", "ufo", "eps", "ai", "raw", "wmf"};
        } else if (type == MEDIA_FILE_TYPE.AUDIO) {
            img = new String[]{"mp3", "wma", "wav", "mod", "ra", "cd", "md", "asf", "aac", "vqf", "ape", "mid", "ogg",
                    "m4a", "vqf"};
        } else if (type == MEDIA_FILE_TYPE.VIDEO) {
            img = new String[]{"mp4", "avi", "mov", "wmv", "asf", "navi", "3gp", "mkv", "f4v", "rmvb", "webm"};
        }
        for (int i = 0; i < img.length; i++) {
            if (img[i].equals(fileType)) {
                return true;
            }
        }
        return false;
    }

    public static MEDIA_FILE_TYPE getFileType(String fileName) {
        // 获取文件后缀名并转化为写，用于后续比较
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        // 创建类型数组
        String[] img = new String[]{"bmp", "jpg", "jpeg", "png", "tiff", "gif", "pcx", "tga", "exif", "fpx", "svg", "psd",
                "cdr", "pcd", "dxf", "ufo", "eps", "ai", "raw", "wmf"};
        for (int i = 0; i < img.length; i++) {
            if (img[i].equals(fileType)) {
                return MEDIA_FILE_TYPE.IMAGE;
            }
        }
        img = new String[]{"mp3", "wma", "wav", "mod", "ra", "cd", "md", "asf", "aac", "vqf", "ape", "mid", "ogg",
                "m4a", "vqf"};
        for (int i = 0; i < img.length; i++) {
            if (img[i].equals(fileType)) {
                return MEDIA_FILE_TYPE.AUDIO;
            }
        }
        img = new String[]{"mp4", "avi", "mov", "wmv", "asf", "navi", "3gp", "mkv", "f4v", "rmvb", "webm"};
        for (int i = 0; i < img.length; i++) {
            if (img[i].equals(fileType)) {
                return MEDIA_FILE_TYPE.VIDEO;
            }
        }
        return null;
    }

}
