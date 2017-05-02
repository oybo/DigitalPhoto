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
import com.xyz.digital.photo.app.bean.UploadInfo;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.EnvironmentUtil;
import com.xyz.digital.photo.app.util.PubUtils;
import com.xyz.digital.photo.app.util.ToastUtil;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void deleteFile(String fileName) {
        if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
            actFileManager.deleteFile(mRemoteCurrentPath + fileName);
        } else {
            actFileManager.deleteFile(mRemoteCurrentPath + "/" + fileName);
        }
    }

    //=====================3删除相关==结束=======================================

    private boolean isResposeFiles;

    public boolean isResposeFiles() {
        return isResposeFiles;
    }

    private ACTFileEventListener actFileEventListener = new ACTFileEventListener() {
        @Override
        public void onOperationProgression(int opcode, int processed, int total) {
            if (opcode == 1) {
                // 上传
                isUpload = true;
                if (mUploadInfo != null) {
                    mUploadInfo.setState(1);
                    mUploadInfo.setProcessed(processed);
                    mUploadInfo.setTotal(total);
                    sendUploadMessage(mUploadInfo);
                }
            } else if (opcode == 2) {
                // 下载
                isDownload = true;
                if (mDownloadInfo != null) {
                    mDownloadInfo.setState(1);
                    mDownloadInfo.setProcessed(processed);
                    mDownloadInfo.setTotal(total);
                    sendDownloadMessage(mDownloadInfo);
                }
            }
        }

        @Override
        public void onUploadCompleted(String remotePath, String localPath, int result) {
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
            if (mDownloadInfo != null) {
                if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                    ActCommunication.getInstance().onUploadFile(remotePath);
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
            boolean success;
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
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
                if (remoteFileList != null) {
                    mRemoteFileList.clear();
                    mRemoteFileList.addAll(remoteFileList);

                    EventBase eventBase = new EventBase();
                    eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
                    EventBus.getDefault().post(eventBase);
                }
            }
            downloadSysConfig();
        }

        @Override
        public void onDeleteDirectoryCompleted(String parentPath, int result) {
            boolean success;
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
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
        EventBase eventBase = new EventBase();
        eventBase.setAction(Constants.SEND_CONNECT_STATE);
        eventBase.setData(success);
        EventBus.getDefault().post(eventBase);
    }

    private AcEventListener mAcEventListener = new AcEventListener() {

        @Override
        public void onDeviceConnected() {
            isConnect = true;
            sendConnectState(true);
        }

        @Override
        public void onDeviceDisconnect() {
            isConnect = false;
            sendConnectState(false);
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
                EventBase eventBase = new EventBase();
                eventBase.setAction(Constants.SEND_MNAD_INFO_ACTION);
                eventBase.setData(status);
                EventBus.getDefault().post(eventBase);
            } else if (action.equals("UdiskInfo")) {
                EventBase eventBase = new EventBase();
                eventBase.setAction(Constants.SEND_UDISK_INFO_ACTION);
                eventBase.setData(status);
                EventBus.getDefault().post(eventBase);
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
     * 下载系统所需的配置文件
     */
    public void downloadSysConfig() {
        if(!downloadSysConfig) {
            try {
                String sysConfigName = "sys_config.cfg";
                File sysFile = new File(EnvironmentUtil.getFilePath(), sysConfigName);
                if(sysFile.exists()) {
                    sysFile.delete();
                }
                int ss = actFileManager.downloadFile("/" + sysConfigName, sysFile.getAbsolutePath());
                downloadSysConfig = (ss == 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
