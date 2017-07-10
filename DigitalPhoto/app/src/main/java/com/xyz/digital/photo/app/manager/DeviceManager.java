package com.xyz.digital.photo.app.manager;

import android.util.Log;
import android.widget.Toast;
import com.actions.actcommunication.AcEventListener;
import com.actions.actcommunication.ActCommunication;
import com.actions.actfilemanager.ACTFileEventListener;
import com.actions.actfilemanager.ActFileInfo;
import com.actions.actfilemanager.ActFileManager;
import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.bean.DownloadInfo;
import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.bean.FileInfo;
import com.xyz.digital.photo.app.bean.ImageInfo;
import com.xyz.digital.photo.app.bean.UploadInfo;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.EnvironmentUtil;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.PubUtils;
import com.xyz.digital.photo.app.util.SysConfigHelper;
import com.xyz.digital.photo.app.util.TimeUtil;
import com.xyz.digital.photo.app.util.ToastUtil;
import org.greenrobot.eventbus.EventBus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by O on 2017/4/25.
 */

public class DeviceManager {

    private static DeviceManager mInstance;

    private boolean mLoginMain;

    /**   作为相框服务器上文件集合    */
    private Map<String, ActFileInfo> mRemoteFileMaps = new HashMap<>();

    private List<ActFileInfo> mRemoteFileList = new ArrayList<>();
    private ActFileManager actFileManager = new ActFileManager();

    public static String mRemoteCurrentPath = "/";

    public static DeviceManager getInstance() {
        if (mInstance == null) {
            synchronized (DeviceManager.class) {
                if (mInstance == null) {
                    mInstance = new DeviceManager();
                }
            }
        }
        return mInstance;
    }

    public String setRemoteCurrentPath(String fileName) {
        String requestPath;
        if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
            requestPath = mRemoteCurrentPath + fileName;
        } else {
            requestPath = mRemoteCurrentPath + "/" + fileName;
        }
        if(isConnect()) {
            actFileManager.browseFiles(requestPath + "/");
        } else {
            ToastUtil.showToast(AppContext.getInstance(), AppContext.getInstance().getSString(R.string.connect_disconnect_txt));
            // 刷新文件列表
            EventBase eventBase = new EventBase();
            eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
            EventBus.getDefault().post(eventBase);
        }
        mRemoteCurrentPath = requestPath;

        return requestPath;
    }

    public void prevRemoteCurrentPath() {
        if(!isConnect()) {
            ToastUtil.showToast(AppContext.getInstance(), AppContext.getInstance().getSString(R.string.connect_disconnect_txt));
            // 刷新文件列表
            EventBase eventBase = new EventBase();
            eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
            EventBus.getDefault().post(eventBase);
            return;
        }
        if (!mRemoteCurrentPath.equalsIgnoreCase("/")) {
            int lastIndex = mRemoteCurrentPath.lastIndexOf("/");
            if (lastIndex == 0) {
                mRemoteCurrentPath = "/";
                actFileManager.browseFiles(mRemoteCurrentPath);
            } else {
                mRemoteCurrentPath = mRemoteCurrentPath.substring(0, lastIndex);
                actFileManager.browseFiles(mRemoteCurrentPath + "/");
            }
        } else {
            actFileManager.browseFiles(mRemoteCurrentPath);
        }
    }

    private MEDIA_SHOW_TYPE mShowType = MEDIA_SHOW_TYPE.CHART;

    public void setShowType(MEDIA_SHOW_TYPE type) {
        mShowType = type;
    }

    public MEDIA_SHOW_TYPE getShowType() {
        return mShowType;
    }

    /**
     * 返回当前相框所有文件
     *
     * @return
     */
    public List<ActFileInfo> getRemoteDeviceFiles() {
//        if(mShowType == MEDIA_SHOW_TYPE.LIST) {
//            return mRemoteFileList;
//        } else {
//            List<ActFileInfo> tempFiles = new ArrayList<>();
//            for(ActFileInfo info : mRemoteFileList) {
//                if(info.getFileType() == ActFileInfo.FILE_TYPE_FILE) {
//                    tempFiles.add(info);
//                }
//            }
//            return tempFiles;
//        }

        return mRemoteFileList;
    }

    public void removeFile(String fileName) {
        for(ActFileInfo actFileInfo : mRemoteFileList) {
            if(actFileInfo.getFileName().equals(fileName)) {
                mRemoteFileList.remove(actFileInfo);
                return;
            }
        }
    }

    //=====================1上传相关==开始=======================================

    private LinkedHashMap<String, UploadInfo> mUploadInfos = new LinkedHashMap<>();
    private UploadInfo mUploadInfo;
    private boolean isUpload;

    public boolean isUploading() {
        return isUpload;
    }

    public void addUpload(int position, String filePath, String fileName) {
        UploadInfo uploadInfo = new UploadInfo(position, filePath, fileName);
        mUploadInfos.put(filePath, uploadInfo);
        // 这里刷新上传状态
        sendUploadMessage(uploadInfo);
    }

    public void removeUpload(String filePath) {
        mUploadInfos.remove(filePath);
    }

    public UploadInfo getCurUploadInfo() {
        return mUploadInfo;
    }

    public boolean isUpload(String filePath) {
        return mUploadInfos.containsKey(filePath);
    }

    public UploadInfo getUploadInfo(String filePath) {
        if (isUpload(filePath)) {
            return mUploadInfos.get(filePath);
        }
        return null;
    }

    public void startUpload() {
        if (mUploadInfos.size() == 0) {
            mUploadInfo = null;
            // 这里刷新下服务器文件列表
            refreshRemoteFiles();
            return;
        }
        if(mRemoteCurrentPath.equals("/")) {
            setRemoteCurrentPath(TimeUtil.getCurToday());
        }
        goUpload();
    }

    private void goUpload() {
        for (Map.Entry<String, UploadInfo> entry : mUploadInfos.entrySet()) {
            // 发送命令查看剩余空间
            String[] msg = new String[]{"cmd", "StorageRemain"};
            ActCommunication.getInstance().sendMsg(msg);

            isUpload = true;
            mUploadInfo = entry.getValue();
            if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
                actFileManager.uploadFile(mUploadInfo.getFilePath(), mRemoteCurrentPath + mUploadInfo.getFileName());
            } else {
                actFileManager.uploadFile(mUploadInfo.getFilePath(), mRemoteCurrentPath + "/" + mUploadInfo.getFileName());
            }
            return;
        }
    }

    private void sendUploadMessage(UploadInfo uploadInfo) {
        EventBase eventBase = new EventBase();
        eventBase.setAction(Constants.SEND_REFRESH_UPLOAD_STATE);
        eventBase.setData(uploadInfo);
        EventBus.getDefault().post(eventBase);
    }

    private void sendDownloadMessage(DownloadInfo downloadInfo) {
        EventBase eventBase = new EventBase();
        eventBase.setAction(Constants.SEND_DOWNLOAD_FILE_RESULT);
        eventBase.setData(downloadInfo);
        EventBus.getDefault().post(eventBase);
    }

    //=====================1上传相关==结束=======================================

    //=====================2下载相关==开始=======================================

    private Map<String, DownloadInfo> mDownloadInfos = new HashMap<>();
    private DownloadInfo mDownloadInfo;
    private boolean isDownload;

    public boolean isDownloading() {
        return isDownload;
    }

    public void addDownload(String fileName, MEDIA_FILE_TYPE type) {
        String localPath = PubUtils.getDonwloadLocalPath(fileName, type);

        DownloadInfo downloadInfo = new DownloadInfo(localPath, fileName);
        mDownloadInfos.put(localPath, downloadInfo);
        // 这里刷新下载状态
        sendDownloadMessage(downloadInfo);
    }

    public void removeDownload(String localPath) {
        mDownloadInfos.remove(localPath);
    }

    public boolean isDownload(String filePath) {
        return mDownloadInfos.containsKey(filePath);
    }

    public DownloadInfo getDownloadInfo(String filePath) {
        if (isDownload(filePath)) {
            return mDownloadInfos.get(filePath);
        }
        return null;
    }

    public void startDownload() {
        if (mDownloadInfos.size() == 0) {
            return;
        }
        for (Map.Entry<String, DownloadInfo> entry : mDownloadInfos.entrySet()) {
            isDownload = true;
            mDownloadInfo = entry.getValue();
            if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
                downloadFile(mRemoteCurrentPath + mDownloadInfo.getFileName(), mDownloadInfo.getFilePath());
            } else {
                downloadFile(mRemoteCurrentPath + "/" + mDownloadInfo.getFileName(), mDownloadInfo.getFilePath());
            }
            return;
        }
    }

    //=====================2下载相关==结束=======================================

    //=====================3删除相关==开始=======================================

    private String mDeleteRemotePath;

    public void deleteDirectory(String fileName) {
        if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
            mDeleteRemotePath = mRemoteCurrentPath + fileName;
        } else {
            mDeleteRemotePath = mRemoteCurrentPath + "/" + fileName;
        }
        actFileManager.deleteDirectory(mDeleteRemotePath);
    }

    public void deleteFile(String fileName) {
        if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
            mDeleteRemotePath = mRemoteCurrentPath + fileName;
        } else {
            mDeleteRemotePath = mRemoteCurrentPath + "/" + fileName;
        }
        actFileManager.deleteFile(mDeleteRemotePath);
    }

    //=====================3删除相关==结束=======================================

    private boolean isResposeFiles;

    public boolean isResposeFiles() {
        return isResposeFiles;
    }

    private ACTFileEventListener actFileEventListener = new ACTFileEventListener() {
        @Override
        public void onOperationProgression(int opcode, int processed, int total) {
            // 进度回调
            if (opcode == 1) {
                // 上传
                if (mUploadInfo != null) {
                    isUpload = true;
                    mUploadInfo.setState(1);
                    mUploadInfo.setProcessed(processed);
                    mUploadInfo.setTotal(total);
                    sendUploadMessage(mUploadInfo);
                }
            } else if (opcode == 2) {
                // 下载
                if (mDownloadInfo != null) {
                    isDownload = true;
                    mDownloadInfo.setState(1);
                    mDownloadInfo.setProcessed(processed);
                    mDownloadInfo.setTotal(total);
                    sendDownloadMessage(mDownloadInfo);
                }
            }
        }

        @Override
        public void onUploadCompleted(String remotePath, String localPath, int result) {
            // 上传完成回调
            File sysFile = new File(EnvironmentUtil.getFilePath(), Constants.SYSTEM_FILE_NAME);
            if(localPath.equals(sysFile.getAbsolutePath())) {
                // 属于系统配置文件
                // 执行刷新命令
                ActCommunication.getInstance().sendMsg(new String[]{"cmd", "cfgfileupdate"});

                if(mSetdateDate != null) {
                    ActCommunication.getInstance().sendMsg(mSetdateDate);
                    mSetdateDate = null;
                }
            }
            if(mUploadInfo != null) {
                if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                    ActCommunication.getInstance().onUploadFile(remotePath);
                    mUploadInfo.setState(2);
                    mUploadInfo.setFilePath(localPath);
                    sendUploadMessage(mUploadInfo);
                    removeUpload(mUploadInfo.getFilePath());
                } else {
                    mUploadInfo.setState(-1);
                    mUploadInfo.setFilePath(localPath);
                    sendUploadMessage(mUploadInfo);
                    removeUpload(mUploadInfo.getFilePath());
                    ToastUtil.showToast(AppContext.getInstance(), mUploadInfo.getFileName() + AppContext.getInstance().getSString(R.string.no_more_data_txt));
                }
                isUpload = false;
            }
            startUpload();
        }

        @Override
        public void onDownloadCompleted(String remotePath, String localPath, int result) {
            localPath = localPath.replace("//", "/");
            if(remotePath.equals("ftp://"+Constants.HOST_IP+"/" + Constants.SYSTEM_FILE_NAME)) {
                // 属于系统配置文件
                downloadSysConfigFile = true;
                readSysConfigFile();
            }

            // 属于临时文件
            if(tempFiles.containsKey(remotePath.replace("ftp://"+Constants.HOST_IP, ""))) {
                tempFilesSum--;
                if(tempFilesSum % 2 == 0 || tempFilesSum <= 0) {
                    EventBase eventBase = new EventBase();
                    eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
                    eventBase.setData("true");
                    EventBus.getDefault().post(eventBase);
                }
            }
            if(mVideoBmpFileMaps.containsKey(localPath)) {
                // 视频缩略图
                if(mVideoBmpFileMaps.size() % 2 == 0 || tempFilesSum <= 0) {
                    EventBase eventBase = new EventBase();
                    eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
                    eventBase.setData("true");
                    EventBus.getDefault().post(eventBase);
                }
                mVideoBmpFileMaps.remove(localPath);
                downloadBmpFiles();
            }
            // 下载回调
            if (mDownloadInfo != null) {
                if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                    mDownloadInfo.setState(2);
                    mDownloadInfo.setFilePath(localPath);
                    sendDownloadMessage(mDownloadInfo);
                    removeDownload(mDownloadInfo.getFilePath());
                    startDownload();
                    ToastUtil.showToast(AppContext.getInstance(), mDownloadInfo.getFileName() + AppContext.getInstance().getSString(R.string.download_ok_txt));
                } else {
                    mDownloadInfo.setState(-1);
                    mDownloadInfo.setFilePath(localPath);
                    sendDownloadMessage(mDownloadInfo);
                    removeDownload(mDownloadInfo.getFilePath());
                    ToastUtil.showToast(AppContext.getInstance(), mDownloadInfo.getFileName() + AppContext.getInstance().getSString(R.string.download_error_txt));
                }
                isDownload = false;
            }
        }

        @Override
        public void onDeleteCompleted(String parentPath, int result) {
            // 删除完成回调
            boolean success;
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                ActCommunication.getInstance().onDeleteFile(mDeleteRemotePath);
                try {
                    String fileName = mDeleteRemotePath.substring(mDeleteRemotePath.lastIndexOf("/") + 1, mDeleteRemotePath.length());
                    removeRemoteFileMap(fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                success = true;
                refreshRemoteFiles();
            } else {
                success = false;
            }
            EventBase eventBase = new EventBase();
            eventBase.setAction(Constants.SEND_DELETE_FILE_RESULT);
            eventBase.setData(success);
            EventBus.getDefault().post(eventBase);

            Toast.makeText(AppContext.getInstance(), success ?
                    AppContext.getInstance().getSString(R.string.delete_success_txt):
                    AppContext.getInstance().getSString(R.string.delete_faild_txt), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBrowseCompleted(Object filelist, String currentPath, int result) {
            mRemoteFileMaps.clear();
            isResposeFiles = true;
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                List<ActFileInfo> remoteFileList = (ArrayList) filelist;
                if(remoteFileList != null && mRemoteCurrentPath.equals("/")) {
                    for(ActFileInfo fileInfo : remoteFileList) {
                        if(fileInfo.getFileName().equals(Constants.SYSTEM_FILE_NAME)) {
                            // 下载系统配置文件
                            downloadSysConfig();
                            continue;
                        }
//                        if(fileInfo.getFileType() == ActFileInfo.FILE_TYPE_DIRECTORY) {
//                            String dirName = TimeUtil.getCurToday();
//                            if(dirName.equals(fileInfo.getFileName())) {
//                                mFristLoadTag = true;
//                                setRemoteCurrentPath(dirName);
//                                return;
//                            }
//                        }
                    }
                }
                if (remoteFileList != null) {
                    mVideoBmpFileMaps.clear();
                    mDownloadTempList.clear();
                    mRemoteFileList.clear();
                    // 下载临时文件
                    for(ActFileInfo actFileInfo : remoteFileList) {
                        String fileName = actFileInfo.getFileName();
                        if(actFileInfo.getFileType() == ActFileInfo.FILE_TYPE_FILE) {
                            mRemoteFileMaps.put(fileName, actFileInfo);
                            // 属于文件，并且是图片类型就下载
                            if(PubUtils.getFileType(fileName) == MEDIA_FILE_TYPE.IMAGE) {
                                String remotePath = DeviceManager.getInstance().getRemotePath(fileName);
                                String localPath = PubUtils.getTempLocalPath(fileName);
                                mDownloadTempList.add(new ImageInfo(remotePath, localPath));
                            }
                        }
                        mRemoteFileList.add(actFileInfo);
                    }

                    // 刷新文件列表
                    EventBase eventBase = new EventBase();
                    eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
                    EventBus.getDefault().post(eventBase);

                    // 下载临时文件
                    downloadTempFiles();
                }
            }
        }

        @Override
        public void onDeleteDirectoryCompleted(String parentPath, int result) {
            boolean success;
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                ActCommunication.getInstance().onDeleteFile(mDeleteRemotePath);
                success = true;
                refreshRemoteFiles();
            } else {
                success = false;
            }
            EventBase eventBase = new EventBase();
            eventBase.setAction(Constants.SEND_DELETE_FILE_RESULT);
            eventBase.setData(success);
            EventBus.getDefault().post(eventBase);

            Toast.makeText(AppContext.getInstance(), success ?
                    AppContext.getInstance().getSString(R.string.delete_success_txt):
                    AppContext.getInstance().getSString(R.string.delete_faild_txt), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCreateDirectoryCompleted(String parentPath, int result) {
        }

        @Override
        public void onDisconnectCompleted(int result) {
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                Toast.makeText(AppContext.getInstance(), AppContext.getInstance().getSString(R.string.connect_disconnect_txt), Toast.LENGTH_SHORT).show();
                connect();
            } else {
            }
        }

        @Override
        public void onRenameCompleted(int result) {

        }
    };

    private void downloadTempFile(ImageInfo imageInfo) {
        String remotePath = imageInfo.getRemotePath();
        String localPath = imageInfo.getLocalPath();
        if(!tempFiles.containsKey(remotePath)) {
            if(!new File(localPath).exists()) {
                File file = new File(localPath);
                File parentFile = new File(file.getParent());
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                tempFiles.put(remotePath, localPath);
                downloadFile(remotePath, localPath);
            }
        }
    }

    public String getRemotePath(String fileName) {
        String path;
        if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
            path = mRemoteCurrentPath + fileName;
        } else {
            path = mRemoteCurrentPath + "/" + fileName;
        }
        return path;
    }

    public synchronized void downloadTempFiles() {
        // 下载临时文件
        for(ImageInfo imageInfo : mDownloadTempList) {
            downloadTempFile(imageInfo);
        }
    }

    public synchronized void downloadBmpFiles() {
        if(mVideoBmpFileMaps.size() > 0) {
            for(Map.Entry<String, ImageInfo> entry : mVideoBmpFileMaps.entrySet()) {
                ImageInfo info = entry.getValue();
                if(info != null) {
                    try {
                        String remotePath = getThumbnailLocalPath(info.getRemotePath());
                        String fileName = remotePath.substring(remotePath.lastIndexOf("/") + 1, remotePath.lastIndexOf(".")) + Constants.VIDEO_BMP_FILE_NAME;
                        String localPath = EnvironmentUtil.getTempFilePath();
                        if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
                            localPath = localPath + mRemoteCurrentPath + fileName;
                        } else {
                            localPath = localPath + mRemoteCurrentPath + "/" + fileName;
                        }
                        localPath = localPath.replace("//", "/");
                        if(!new File(localPath).exists()) {
                            ActCommunication.getInstance().requestThumbnails(info.getRemotePath());
                        } else {
                            mVideoBmpFileMaps.remove(localPath);
                            downloadBmpFiles();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ActCommunication.getInstance().requestThumbnails(info.getRemotePath());
                    }
                    return;
                }
            }
        }
    }

    public void addVideoBmpFileMaps(String fileName) {
        String remotePath = DeviceManager.getInstance().getRemotePath(fileName);
        String localPath = PubUtils.getTempLocalPath(fileName);
        mVideoBmpFileMaps.put(localPath, new ImageInfo(remotePath, localPath));
    }

    private Map<String, ImageInfo> mVideoBmpFileMaps = new HashMap<>();

    private List<ImageInfo> mDownloadTempList = new ArrayList<>();

    private Map<String, String> tempFiles = new HashMap<>();
    private int tempFilesSum;

    private void refreshRemoteFiles() {
        if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
            actFileManager.browseFiles(mRemoteCurrentPath);
        } else {
            actFileManager.browseFiles(mRemoteCurrentPath + "/");
        }
    }

    public void addRemoteFileMap(String fileName) {
        mRemoteFileMaps.put(fileName, null);
    }

    public void removeRemoteFileMap(String fileName) {
        mRemoteFileMaps.remove(fileName);
    }

    public boolean isExist(String fileName) {
        return mRemoteFileMaps.containsKey(fileName);
    }

    public void disConnect() {
        isConnect = false;
        mLoginMain = false;
        propertiesMap.clear();
        mRemoteFileList.clear();
//        ActCommunication.getInstance().disconnect();
//        actFileManager.disconnect();
    }

    /**
     * 登录那里的连接
     */
    public void connect() {
        // 连接服务器的消息通讯服务，连接成功后才能发送消息
        ActCommunication.getInstance().setEventListener(mAcEventListener);
        ActCommunication.getInstance().connect(Constants.HOST_IP);
        // 连接服务区的文件服务
        actFileManager.registerEventListener(actFileEventListener);
        actFileManager.connect(Constants.HOST_IP);
        actFileManager.browseFiles(mRemoteCurrentPath);
    }

    public void refreshBrowseFiles() {
        try {
            mRemoteCurrentPath = "/";
            actFileManager.browseFiles(mRemoteCurrentPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isConnect;

    public boolean isConnect() {
        return isConnect;
    }

    private void sendConnectState(boolean success) {
        if(isConnect) {
            return;
        }
        EventBase eventBase = new EventBase();
        eventBase.setAction(Constants.SEND_CONNECT_STATE);
        eventBase.setData(success);
        EventBus.getDefault().post(eventBase);
    }

    private AcEventListener mAcEventListener = new AcEventListener() {

        @Override
        public void onDeviceConnected() {
            Toast.makeText(AppContext.getInstance(), AppContext.getInstance().getSString(R.string.connect_success_txt), Toast.LENGTH_SHORT).show();
            sendConnectState(true);
            isConnect = true;
            // 新建今天日期命名文件夹
            actFileManager.createDirectory(TimeUtil.getCurToday());
        }

        @Override
        public void onDeviceDisconnect() {
            Toast.makeText(AppContext.getInstance(), AppContext.getInstance().getSString(R.string.connect_disconnect_txt), Toast.LENGTH_SHORT).show();
            sendConnectState(false);
            isConnect = false;
        }

        @Override
        public void onRecvVolume(int volume) {
            for (OnCmdBackListener listener : mCmdListeners) {
                listener.onVolume(volume);
            }
        }

        @Override
        public void onRecvTotalTime(int timeMs) {
        }

        @Override
        public void onRecvCurrentTime(int timeMs) {
        }

        @Override
        public void onRecvPlayerStatus(int status) {
        }

        @Override
        public void onRecvPlaySequence(int seq) {
        }

        @Override
        public void onRecvThumbnail(String url, byte[] data) {
            // 接收到图片缩略图
            Log.e("sssssssssss=======", "url= " + url);
        }

        @Override
        public void onRecvResult(String action, String[] status) {
            if (action.equals("NandInfo")) {
                // 内部存储信息
                EventBase eventBase = new EventBase();
                eventBase.setAction(Constants.SEND_MNAD_INFO_ACTION);
                eventBase.setData(status);
                EventBus.getDefault().post(eventBase);
            } else if (action.equals("UdiskInfo")) {
                // U盘存储信息
                EventBase eventBase = new EventBase();
                eventBase.setAction(Constants.SEND_UDISK_INFO_ACTION);
                eventBase.setData(status);
                EventBus.getDefault().post(eventBase);
            } else if (action.equals("brightness")) {
                // 遥控器的亮度
                for (OnCmdBackListener listener : mCmdListeners) {
                    listener.onBrightness(Integer.parseInt(status[3]));
                }
            } else if(action.equals("storageInfo")) {
                String size = status[3];
                long allSize = PubUtils.conversionSize(size);
                if(allSize < 100) {
                    // 空间不足
                    ToastUtil.showToast(AppContext.getInstance(), AppContext.getInstance().getSString(R.string.residue_device_size_txt) + size);
                }
            }
        }

        @Override
        public void ThumbnailReady(String remotePath, boolean isReplyFailed) {
            if(isReplyFailed) {
                int count = 0;
                if(tempMapss.containsKey(remotePath)) {
                    count = tempMapss.get(remotePath);
                    if(count > 3) {

                        String t_remotePath = getThumbnailLocalPath(remotePath);
                        String t_localPath = EnvironmentUtil.getTempFilePath() + File.separator + t_remotePath.replace(Constants.VIDEO_BMP_NAME, "");
                        t_localPath = t_localPath.replace("//", "/");

                        mVideoBmpFileMaps.remove(t_localPath);
                        downloadBmpFiles();

                        return;
                    }
                    count++;
                }
                tempMapss.put(remotePath, count);
                // 重新发起视频缩略图请求
                downloadBmpFiles();
                return;
            }

            remotePath = getThumbnailLocalPath(remotePath);
            String localPath = EnvironmentUtil.getTempFilePath() + File.separator + remotePath.replace(Constants.VIDEO_BMP_NAME, "");
            localPath = localPath.replace("//", "/");
            if(!new File(localPath).exists()) {
                File file = new File(localPath);
                File parentFile = new File(file.getParent());
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                downloadFile(remotePath, localPath);
            } else {
                mVideoBmpFileMaps.remove(localPath);
                downloadBmpFiles();
            }
        }
    };

    private String getThumbnailLocalPath(String remotePath) {
        try {
            if(remotePath.split("/").length > 2) {
                remotePath = remotePath.substring(remotePath.indexOf("/") + 1, remotePath.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(remotePath.split("/").length > 2) {
                remotePath = remotePath.substring(remotePath.indexOf("/") + 1, remotePath.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(remotePath.split("/").length > 2) {
                remotePath = remotePath.substring(remotePath.indexOf("/"), remotePath.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return remotePath;
    }

    private Map<String, Integer> tempMapss = new HashMap<>();

    private List<OnCmdBackListener> mCmdListeners = new ArrayList<>();

    public void addOnCmdBackListener(OnCmdBackListener listener) {
        if (listener != null) {
            mCmdListeners.add(listener);
        }
    }

    public interface OnCmdBackListener {
        void onVolume(int value);
        void onBrightness(int value);
    }

    private LinkedHashMap<String, String> propertiesMap = new LinkedHashMap<String, String>();

    private boolean downloadSysConfigFile;

    private void downloadFile(String remotePath, String localPath) {
        actFileManager.downloadFile(remotePath, localPath);
    }

    /**
     * 下载系统配置文件
     */
    public void downloadSysConfig() {
        if(!downloadSysConfigFile) {
            try {
                File sysFile = new File(EnvironmentUtil.getFilePath(), Constants.SYSTEM_FILE_NAME);
                downloadFile("/" + Constants.SYSTEM_FILE_NAME, sysFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void readSysConfigFile() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                File sysFile = new File(EnvironmentUtil.getFilePath(), Constants.SYSTEM_FILE_NAME);
                String read;
                FileReader fileread = null;
                try {
                    fileread = new FileReader(sysFile);
                    BufferedReader bufread = new BufferedReader(fileread);
                    try {
                        while ((read = bufread.readLine()) != null) {
                            try {
                                String[] strs = read.split("=");
                                propertiesMap.put(strs[0], strs[1]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if(fileread != null) {
                        try {
                            fileread.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }

    /**
     * 上传系统配置文件
     */
    public void uploadSysConfig() {
        try {
            File sysFile = new File(EnvironmentUtil.getFilePath(), Constants.SYSTEM_FILE_NAME);
            int ss = actFileManager.uploadFile(sysFile.getAbsolutePath(), "/" + Constants.SYSTEM_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getpropertiesValue(String key) {
        if(propertiesMap.size() == 0) {
            readSysConfigFile();
        }
        if(!propertiesMap.containsKey(key)) {
            return "0";
        }
        return propertiesMap.get(key);
    }

    /**
     * 通过语言Code获取对应语言名称
     * @param lanCode
     * @return
     */
    public String getLanguageStr(String lanCode) {
        boolean isChian;
        int id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mSelectLanguage_key, 0);
        if (id == 1) {
            // 英文
            isChian = false;
        } else {
            // 中文
            isChian = true;
        }

        String result = "";
        if(lanCode.equals("zh_CN")) {
            result = isChian ? "简体中文" : "Simplified Chinese";
        }
        else if(lanCode.equals("zh_TW")) {
            result = isChian ? "繁體中文" : "traditional Chinese";
        }
        else if(lanCode.equals("en_GB")) {
            result = isChian ? "英语" : "English";
        }
        else if(lanCode.equals("ja_JP")) {
            result = isChian ? "日语" : "Japanese";
        }
        else if(lanCode.equals("ko_KP")) {
            result = isChian ? "韩语" : "Korean";
        }
        else if(lanCode.equals("fr_FR")) {
            result = isChian ? "法语" : "French";
        }
        else if(lanCode.equals("de_GE")) {
            result = isChian ? "德语" : "German";
        }
        else if(lanCode.equals("it_IT")) {
            result = isChian ? "意大利语" : "Italian";
        }
        else if(lanCode.equals("nl_NL")) {
            result = isChian ? "荷兰语" : "Dutch";
        }
        else if(lanCode.equals("pt_PT")) {
            result = isChian ? "葡萄牙语" : "Portuguese";
        }
        else if(lanCode.equals("es_PE")) {
            result = isChian ? "西班牙语" : "Spanish";
        }
        else if(lanCode.equals("sv_SE")) {
            result = isChian ? "瑞典语" : "Swedish";
        }
        else if(lanCode.equals("cs_CZ")) {
            result = isChian ? "捷克语" : "Czech";
        }
        else if(lanCode.equals("da_DK")) {
            result = isChian ? "丹麦语" : "Danish";
        }
        else if(lanCode.equals("pl_PL")) {
            result = isChian ? "波兰语" : "Polish";
        }
        else if(lanCode.equals("ru_RU")) {
            result = isChian ? "俄语" : "Russian";
        }
        else if(lanCode.equals("tr_TR")) {
            result = isChian ? "土耳其语" : "Turkish";
        }
        else if(lanCode.equals("he_IL")) {
            result = isChian ? "希伯来语" : "Hebrew";
        }
        else if(lanCode.equals("th_TH")) {
            result = isChian ? "泰国语言" : "Thai Language";
        }
        else if(lanCode.equals("hu_HU")) {
            result = isChian ? "匈牙利" : "Hungary";
        }
        else if(lanCode.equals("sk_SK")) {
            result = isChian ? "斯洛伐克语" : "Slovakian";
        }
        else if(lanCode.equals("ar")) {
            result = isChian ? "阿拉伯语" : "Arabic";
        }
        else if(lanCode.equals("id_ID")) {
            result = isChian ? "印度尼西亚" : "Indonesia";
        }
        else if(lanCode.equals("el_EG")) {
            result = isChian ? "希腊语" : "Greek";
        }
        else if(lanCode.equals("vi_VI")) {
            result = isChian ? "越南语" : "Vietnamese";
        }
        else if(lanCode.equals("ms_MY")) {
            result = isChian ? "马来语" : "Malay";
        }
        else if(lanCode.equals("hi_HI")) {
            result = isChian ? "印第语" : "Hindi";
        }
        else if(lanCode.equals("no_NO")) {
            result = isChian ? "挪威语" : "Norwegian";
        }
        else if(lanCode.equals("se_FI")) {
            result = isChian ? "芬兰语" : "Finnish";
        }
        else if(lanCode.equals("ro_RO")) {
            result = isChian ? "罗马尼亚" : "Romania";
        }
        return result;
    }

    private String[] mSetdateDate;

    public void sendSetdateDate(String[] data) {
        mSetdateDate = data;
    }

    public void setpropertiesValue(String key, String value) {
        try {
            // 刷新
            propertiesMap.put(key, value);
            // 修改文件
            saveTxtByStr();
            // 上传
            uploadSysConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** */
    /**
     */
    private void saveTxtByStr() {
        OutputStreamWriter write = null;
        BufferedWriter writer = null;
        try {
            File sysFile = new File(EnvironmentUtil.getFilePath(), Constants.SYSTEM_FILE_NAME);
            sysFile.delete();
            sysFile.createNewFile();
            write = new OutputStreamWriter(new FileOutputStream(sysFile), "UTF-8");
            writer = new BufferedWriter(write);
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                writer.append(entry.getKey() + "=" + entry.getValue() + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null) {
                    writer.close();
                }
                if(write != null) {
                    write.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**   ====================添加播放文件=========================  */

    private Map<String, String> mPlayFiles = new HashMap<>();

    public void setPlayFile(FileInfo fileInfo) {
        MEDIA_FILE_TYPE type = fileInfo.getType();
        int fileType = ActCommunication.FILE_TYPE_PHOTO;

        if (type == MEDIA_FILE_TYPE.IMAGE) {
            // 图片
            fileType = ActCommunication.FILE_TYPE_PHOTO;
        } else if (type == MEDIA_FILE_TYPE.VIDEO) {
            // 视频
            fileType = ActCommunication.FILE_TYPE_MUSIC;
        } else if (type == MEDIA_FILE_TYPE.AUDIO) {
            // 音乐
            fileType = ActCommunication.FILE_TYPE_VIDEO;
        }
        if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
            ActCommunication.getInstance().playFile(mRemoteCurrentPath + fileInfo.getFileName(), fileType);
        } else {
            ActCommunication.getInstance().playFile(mRemoteCurrentPath + "/" + fileInfo.getFileName(), fileType);
        }

        addPlayFile(mRemoteCurrentPath + fileInfo.getFileName());
    }

    private void addPlayFile(String filePath) {
        mPlayFiles.clear();
        mPlayFiles.put(filePath, filePath);
    }

    public boolean isPlay(String filePath) {
        return mPlayFiles.containsKey(filePath);
    }

}
