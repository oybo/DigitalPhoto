package com.xyz.digital.photo.app.bean;

import com.xyz.digital.photo.app.mvp.Photo.PhotoContract;

import java.io.Serializable;

/**
 * Created by O on 2017/3/21.
 */

public class MediaFileBean implements Serializable {

    // 名称
    private String fileName;
    // 文件路径
    private String filePath;
    private String size;
    private String date;
    // 类型
    private PhotoContract.MEDIA_FILE_TYPE fileType;
    private int position;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public PhotoContract.MEDIA_FILE_TYPE getFileType() {
        return fileType;
    }

    public void setFileType(PhotoContract.MEDIA_FILE_TYPE fileType) {
        this.fileType = fileType;
    }
}
