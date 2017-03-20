package com.xyz.digital.photo.app.bean;

/**
 * Created by O on 2017/3/20.
 */

public class VideoBean {

    // 名称
    private String title;
    // 类型
    private String mimeType;
    // 文件路径
    private String filePath;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
