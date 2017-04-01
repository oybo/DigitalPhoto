package com.xyz.digital.photo.app.bean;

import com.xyz.digital.photo.app.mvp.Photo.PhotoContract;

import java.io.Serializable;

/**
 * Created by O on 2017/3/21.
 */

public class MediaFileBean implements Serializable{

    // 名称
    private String fileName;
    // 文件路径
    private String filePath;
    // 类型
    private PhotoContract.MEDIA_FILE_TYPE fileType;

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

    public PhotoContract.MEDIA_FILE_TYPE getFileType() {
        return fileType;
    }

    public void setFileType(PhotoContract.MEDIA_FILE_TYPE fileType) {
        this.fileType = fileType;
    }
}
