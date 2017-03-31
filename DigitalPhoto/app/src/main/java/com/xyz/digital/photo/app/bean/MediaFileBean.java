package com.xyz.digital.photo.app.bean;

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
    private Media_FILE_TYPE fileType;

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

    public Media_FILE_TYPE getFileType() {
        return fileType;
    }

    public void setFileType(Media_FILE_TYPE fileType) {
        this.fileType = fileType;
    }
}
