package com.xyz.digital.photo.app.manager;

import android.util.Log;
import android.widget.Toast;

import com.actions.actcommunication.AcEventListener;
import com.actions.actcommunication.ActCommunication;
import com.actions.actfilemanager.ACTFileEventListener;
import com.actions.actfilemanager.ActFileInfo;
import com.actions.actfilemanager.ActFileManager;
import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.bean.DownloadInfo;
import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.bean.FileInfo;
import com.xyz.digital.photo.app.bean.UploadInfo;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.EnvironmentUtil;
import com.xyz.digital.photo.app.util.PubUtils;
import com.xyz.digital.photo.app.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.actions.actfilemanager.ActFileManager.downloadFile;

/**
 * Created by O on 2017/4/25.
 */

public class DeviceManager {

    private static DeviceManager mInstance;

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
        actFileManager.browseFiles(requestPath + "/");
        mRemoteCurrentPath = requestPath;

        return requestPath;
    }

    public void prevRemoteCurrentPath() {
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

    /**
     * 返回当前相框所有文件
     *
     * @return
     */
    public List<ActFileInfo> getRemoteDeviceFiles() {
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

    private Map<String, UploadInfo> mUploadInfos = new HashMap<>();
    private UploadInfo mUploadInfo;
    private boolean isUpload;

    public boolean isUploading() {
        return isUpload;
    }

    public void addUpload(String filePath, String fileName) {
        UploadInfo uploadInfo = new UploadInfo(filePath, fileName);
        mUploadInfos.put(filePath, uploadInfo);
        // 这里刷新上传状态
        sendUploadMessage(uploadInfo);
    }

    public void removeUpload(String filePath) {
        mUploadInfos.remove(filePath);
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
            // 上传完成后命令回调一下
            ActCommunication.getInstance().onUploadFinish();
            // 这里刷新下服务器文件列表
            refreshRemoteFiles();
            return;
        }
        for (Map.Entry<String, UploadInfo> entry : mUploadInfos.entrySet()) {
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
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                ActCommunication.getInstance().onUploadFile(remotePath);
                mUploadInfo.setState(2);
                mUploadInfo.setFilePath(localPath);
                sendUploadMessage(mUploadInfo);
                removeUpload(mUploadInfo.getFilePath());
                startUpload();
            } else {
                mUploadInfo.setState(-1);
                mUploadInfo.setFilePath(localPath);
                sendUploadMessage(mUploadInfo);
                removeUpload(mUploadInfo.getFilePath());
                ToastUtil.showToast(AppContext.getInstance(), mUploadInfo.getFileName() + "上传失败");
            }
            isUpload = false;
        }

        @Override
        public void onDownloadCompleted(String remotePath, String localPath, int result) {
            if(tempFiles.containsKey(remotePath)) {
                // 属于临时文件
                if (result != ACTFileEventListener.OPERATION_SUCESSFULLY) {
                    tempFiles.remove(remotePath);
                }
                tempFileSize--;
                if(tempFileSize <= 0) {
                    EventBase eventBase = new EventBase();
                    eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
                    EventBus.getDefault().post(eventBase);
                }
                return;
            }
            // 下载回调
            if (mDownloadInfo != null) {
                if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                    mDownloadInfo.setState(2);
                    mDownloadInfo.setFilePath(localPath);
                    sendDownloadMessage(mDownloadInfo);
                    removeDownload(mDownloadInfo.getFilePath());
                    startDownload();
                    ToastUtil.showToast(AppContext.getInstance(), mDownloadInfo.getFileName() + "下载成功");

                } else {
                    mDownloadInfo.setState(-1);
                    mDownloadInfo.setFilePath(localPath);
                    sendDownloadMessage(mDownloadInfo);
                    removeDownload(mDownloadInfo.getFilePath());
                    ToastUtil.showToast(AppContext.getInstance(), mDownloadInfo.getFileName() + "下载失败");
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
                success = true;
                refreshRemoteFiles();
            } else {
                success = false;
            }
            EventBase eventBase = new EventBase();
            eventBase.setAction(Constants.SEND_DELETE_FILE_RESULT);
            eventBase.setData(success);
            EventBus.getDefault().post(eventBase);

            Toast.makeText(AppContext.getInstance(), success ? "删除成功" : "删除失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBrowseCompleted(Object filelist, String currentPath, int result) {
            isResposeFiles = true;
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                List<ActFileInfo> remoteFileList = (ArrayList) filelist;
                for(ActFileInfo fileInfo : remoteFileList) {
                    if(fileInfo.getFileName().equals(Constants.SYSTEM_FILE_NAME)) {
                        remoteFileList.remove(fileInfo);
                        break;
                    }
                }
                if (remoteFileList != null) {
                    mRemoteFileList.clear();
                    mRemoteFileList.addAll(remoteFileList);
                    // 更新文件系统
                    EventBase eventBase = new EventBase();
                    eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
                    EventBus.getDefault().post(eventBase);
                    // 下载系统配置文件
                    downloadSysConfig();
                    // 下载临时文件
                    for(ActFileInfo actFileInfo : mRemoteFileList) {
                        if(actFileInfo.getFileType() == ActFileInfo.FILE_TYPE_FILE) {
                            // 属于文件，并且是图片类型就下载
                            if(PubUtils.getFileType(actFileInfo.getFileName()) == MEDIA_FILE_TYPE.IMAGE) {
                                // 下载临时文件
                                String remotePath = mRemoteCurrentPath + actFileInfo.getFileName();
                                String localPath = PubUtils.getTempLocalPath(actFileInfo.getFileName(), MEDIA_FILE_TYPE.IMAGE);
                                if(!tempFiles.containsKey(remotePath)) {
                                    tempFiles.put(remotePath, localPath);
                                    if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
                                        downloadFile(mRemoteCurrentPath + actFileInfo.getFileName(), localPath);
                                    } else {
                                        downloadFile(mRemoteCurrentPath + "/" + actFileInfo.getFileName(), localPath);
                                    }
                                }
                            }
                        }
                    }
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

            Toast.makeText(AppContext.getInstance(), success ? "删除成功" : "删除失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCreateDirectoryCompleted(String parentPath, int result) {
        }

        @Override
        public void onDisconnectCompleted(int result) {
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                Toast.makeText(AppContext.getInstance(), "连接已断开", Toast.LENGTH_SHORT).show();
                connect();
            } else {
            }
        }
    };

    private Map<String, String> tempFiles = new HashMap<>();
    private int tempFileSize;

    public String getTempFile(String remotePath) {
        if(tempFiles.containsKey(remotePath)) {
            return tempFiles.get(remotePath);
        }
        return null;
    }

    private void refreshRemoteFiles() {
        if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
            actFileManager.browseFiles(mRemoteCurrentPath);
        } else {
            actFileManager.browseFiles(mRemoteCurrentPath + "/");
        }
    }

    public void disConnect() {
        isConnect = false;
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
            ToastUtil.showToast(AppContext.getInstance(), "连接成功");
            sendConnectState(true);
            isConnect = true;
        }

        @Override
        public void onDeviceDisconnect() {
            ToastUtil.showToast(AppContext.getInstance(), "连接已断开");
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
            }
        }
    };

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

    private boolean downloadSysConfig;

    /**
     * 下载系统配置文件
     */
    public void downloadSysConfig() {
        if(!downloadSysConfig) {
            try {
                File sysFile = new File(EnvironmentUtil.getFilePath(), Constants.SYSTEM_FILE_NAME);
                if(sysFile.exists()) {
                    sysFile.delete();
                }
                int ss = actFileManager.downloadFile("/" + Constants.SYSTEM_FILE_NAME, sysFile.getAbsolutePath());
                downloadSysConfig = (ss == 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 上传系统配置文件
     */
    public void uploadSysConfig() {
        try {
            File sysFile = new File(EnvironmentUtil.getFilePath(), Constants.SYSTEM_FILE_NAME);
            if(sysFile.exists()) {
                sysFile.delete();
            }
            int ss = actFileManager.uploadFile(sysFile.getAbsolutePath(), "/" + Constants.SYSTEM_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> propertiesMap = new HashMap<String, String>();

    private void getProperties() {
        try {
            Properties properties = new Properties();
            File sysFile = new File(EnvironmentUtil.getFilePath(), Constants.SYSTEM_FILE_NAME);
            FileInputStream fis = new FileInputStream(sysFile);
            properties.load(fis);
            Set<Object> keySet = properties.keySet();
            for (Object object : keySet) {
                String key = (String) object;
                String value = (String) properties.get(key);
                System.out.println(key + "=" + value);

                String s = new String(value.getBytes(), "UTF-8");
                String ss = new String(value.getBytes(), "GBK");
                System.out.println(s + "=" + ss);
                propertiesMap.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getpropertiesValue(String key) {
        if(propertiesMap.size() == 0) {
            getProperties();
        }
        String value = "";
        value = propertiesMap.get(key);

        return value;
    }

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
