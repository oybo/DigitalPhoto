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

    // 类型
    private MEDIA_FILE_TYPE fileType;

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

    public MEDIA_FILE_TYPE getFileType() {
        return fileType;
    }

    public void setFileType(MEDIA_FILE_TYPE fileType) {
        this.fileType = fileType;
    }
}
