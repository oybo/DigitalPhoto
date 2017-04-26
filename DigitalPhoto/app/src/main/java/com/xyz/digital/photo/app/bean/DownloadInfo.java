package com.xyz.digital.photo.app.bean;


/**
 * Created by O on 2017/4/26.
 */

public class DownloadInfo {

    private String filePath;
    private String fileName;
    private int processed;
    private int total;
    private int state;  // 0 = 等待下载，1 = 下载中, -1 = 上传出错， 2 = 上传成功

    public DownloadInfo(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getProcessed() {
        return processed;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

}
