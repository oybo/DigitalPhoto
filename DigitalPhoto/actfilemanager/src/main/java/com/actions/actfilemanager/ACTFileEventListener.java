package com.actions.actfilemanager;

/**
 * EventListener for ActFileManager
 */
public interface ACTFileEventListener {

    public static final int OPERATION_SUCESSFULLY = 0;
    public static final int OPERATION_FAILED = -1;

    /**
     * Called when upload is download
     *
     * @param opcode    operation type, upload or download
     * @param processed downloaded or uploaded bytes of current operation
     * @param total     download or upload total bytes
     */
    public void onOperationProgression(int opcode, int processed, int total);

    /**
     * Called when upload is done
     *
     * @param path   upload path
     * @param result upload result
     */
    public void onUploadCompleted(String remotePath,String localPath, int result);

    /**
     * Called when download is done
     *
     * @param remotePath the file's remote path on the remote server
     * @param localPath  the path to store downloaded file
     */
    public void onDownloadCompleted(String remotePath, String localPath,int result);

    /**
     * Called when delete is done
     *
     * @param parentPath the parent path of the file
     * @param result     delete result
     */
    public void onDeleteCompleted(String parentPath, int result);

    /**
     * Called when browse is done
     *
     * @param filelist is ArrayList of ActFileInfo
     */
    public void onBrowseCompleted(Object filelist, String currentPath,int result);

    /**
     * Called when delete directory is done
     *
     * @param parentPath the parent path of the directory
     * @param result     delete result
     */
    public void onDeleteDirectoryCompleted(String parentPath, int result);

    /**
     * Called when create directory is done
     *
     * @param parentPath the parent path of the directory
     * @param result     delete result
     */
    public void onCreateDirectoryCompleted(String parentPath, int result);

    /**
     * Called when try to disconnect with the remote
     * @param result disconnect result
     */
    public void onDisconnectCompleted(int result);

    /**
     * Called when rename is done
     *
     * @param result rename result
     */
    public void onRenameCompleted(int result);
}
