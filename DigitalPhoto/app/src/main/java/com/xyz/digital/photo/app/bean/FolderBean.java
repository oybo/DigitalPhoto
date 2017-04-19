package com.xyz.digital.photo.app.bean;

import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;

/**
 * GridView的每个item的数据对象
 *
 * @author len
 */
public class FolderBean {
    /**
     * 文件夹的第一张图片路径
     */
    private String topImagePath;
    /**
     * 文件夹名
     */
    private String folderName;
    /**
     * 文件夹中的图片数
     */
    private int imageCounts;
    private String size;
    private String date;
    // 类型 是否是文件夹
    private boolean folder;
    // 类型
    private MEDIA_FILE_TYPE fileType;
    private int position;

    public String getTopImagePath() {
        return topImagePath;
    }

    public void setTopImagePath(String topImagePath) {
        this.topImagePath = topImagePath;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getImageCounts() {
        return imageCounts;
    }

    public void setImageCounts(int imageCounts) {
        this.imageCounts = imageCounts;
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

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public MEDIA_FILE_TYPE getFileType() {
        return fileType;
    }

    public void setFileType(MEDIA_FILE_TYPE fileType) {
        this.fileType = fileType;
    }
}
