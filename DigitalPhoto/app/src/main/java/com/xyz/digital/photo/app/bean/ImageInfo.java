package com.xyz.digital.photo.app.bean;

/**
 * Created by O on 2017/5/19.
 */

public class ImageInfo {

    private String remotePath;
    private String localPath;

    public ImageInfo (String remotePath, String localPath) {
        this.remotePath = remotePath;
        this.localPath = localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
