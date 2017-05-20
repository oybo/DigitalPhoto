package com.xyz.digital.photo.app.bean;

import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;

/**
 * Created by O on 2017/4/26.
 */

public class FileInfo {

    private int position;
    private String fileName;
    private int fileType;
    private MEDIA_FILE_TYPE type;

    private String mModifyTime;
    private int mFileSize;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public MEDIA_FILE_TYPE getType() {
        return type;
    }

    public void setType(MEDIA_FILE_TYPE type) {
        this.type = type;
    }

    public String getmModifyTime() {
        return mModifyTime;
    }

    public void setmModifyTime(String mModifyTime) {
        this.mModifyTime = mModifyTime;
    }

    public int getmFileSize() {
        return mFileSize;
    }

    public void setmFileSize(int mFileSize) {
        this.mFileSize = mFileSize;
    }
}
