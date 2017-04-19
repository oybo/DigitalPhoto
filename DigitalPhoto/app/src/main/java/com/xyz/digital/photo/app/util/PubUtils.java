package com.xyz.digital.photo.app.util;

import java.io.File;

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

}
