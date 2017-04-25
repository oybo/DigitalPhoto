package com.xyz.digital.photo.app.manager;

import android.util.Log;

import com.actions.actcommunication.AcEventListener;
import com.actions.actcommunication.ActCommunication;
import com.actions.actfilemanager.ACTFileEventListener;
import com.actions.actfilemanager.ActFileInfo;
import com.actions.actfilemanager.ActFileManager;
import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.bean.UploadInfo;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by O on 2017/4/25.
 */

public class DeviceManager {

    private static final String LOG_TAG = "=======DeviceManager=======";

    private static DeviceManager mInstance;

    private List<ActFileInfo> mRemoteFileList;
    private ActFileManager actFileManager;
    private static String mRemoteCurrentPath = "/";

    public static DeviceManager getInstance() {
        if(mInstance == null) {
            synchronized (DeviceManager.class) {
                if(mInstance == null) {
                    mInstance = new DeviceManager();
                }
            }
        }
        return mInstance;
    }

    private DeviceManager() {
        mRemoteFileList = new ArrayList<>();
        actFileManager = new ActFileManager();
    }

    /**         设备的IP连接 管理文件                */
    public void connectIP() {
        actFileManager.registerEventListener(new MyACTFileEventListener());
        actFileManager.connect(Constants.HOST_IP);
        actFileManager.browseFiles(mRemoteCurrentPath);
    }

    public List<ActFileInfo> getRemoteDeviceFiles() {
        return mRemoteFileList;
    }

    private Map<String, UploadInfo> mUploadInfos = new HashMap<>();

    public boolean isUploading() {
        return mUploadInfos.size() > 0;
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
        if(isUpload(filePath)) {
            return mUploadInfos.get(filePath);
        }
        return null;
    }

    private UploadInfo mUploadInfo;

    public void startUpload() {
        if(mUploadInfos.size() == 0) {
            mUploadInfo = null;
            return;
        }
        for(Map.Entry<String, UploadInfo> entry : mUploadInfos.entrySet()) {
            mUploadInfo = entry.getValue();
            actFileManager.uploadFile(mUploadInfo.getFilePath(), "/" + mUploadInfo.getFileName());
            return;
        }
    }

    private void sendUploadMessage(UploadInfo uploadInfo) {
        EventBase eventBase = new EventBase();
        eventBase.setAction(Constants.SEND_REFRESH_UPLOAD_STATE);
        eventBase.setData(uploadInfo);
        EventBus.getDefault().post(eventBase);
    }

    public class MyACTFileEventListener implements ACTFileEventListener {
        @Override
        public void onOperationProgression(int opcode, int processed, int total) {
            Log.d(LOG_TAG, "opcode = " + opcode + " and processed " + processed + " among " + total);
            mUploadInfo.setState(1);
            mUploadInfo.setProcessed(processed);
            mUploadInfo.setTotal(total);
            sendUploadMessage(mUploadInfo);
        }

        @Override
        public void onUploadCompleted(String remotePath, String localPath, int result) {
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                ActCommunication.getInstance().onUploadFile(remotePath);
                Log.d(LOG_TAG, "upload success: " + remotePath);
                mUploadInfo.setState(2);
                sendUploadMessage(mUploadInfo);
                mUploadInfos.remove(mUploadInfo.getFilePath());
                startUpload();
            } else {
                mUploadInfo.setState(-1);
                sendUploadMessage(mUploadInfo);
                mUploadInfos.remove(mUploadInfo.getFilePath());
                ToastUtil.showToast(AppContext.getInstance(), mUploadInfo.getFileName() + "上传失败");
            }
        }

        @Override
        public void onDownloadCompleted(String remotePath, String localPath, int result) {
        }

        @Override
        public void onDeleteCompleted(String parentPath, int result) {
        }

        @Override
        public void onBrowseCompleted(Object filelist, String currentPath, int result) {
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                List<ActFileInfo> remoteFileList = (ArrayList) filelist;
                if(remoteFileList != null) {
                    mRemoteFileList.clear();
                    mRemoteFileList.addAll(remoteFileList);

                    EventBase eventBase = new EventBase();
                    eventBase.setAction(Constants.REFRESH_DEVICE_FILE);
                    EventBus.getDefault().post(eventBase);
                }
            }
        }

        @Override
        public void onDeleteDirectoryCompleted(String parentPath, int result) {
        }

        @Override
        public void onCreateDirectoryCompleted(String parentPath, int result) {
        }

        @Override
        public void onDisconnectCompleted(int result) {
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
        // 连接监听
        ActCommunication.getInstance().setEventListener(mAcEventListener);
        ActCommunication.getInstance().connect(Constants.HOST_IP);
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
            DeviceManager.getInstance().connectIP();
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
        }

        @Override
        public void onRecvResult(String action, String[] status) {
            if(action.equals("NandInfo")) {
                EventBase eventBase = new EventBase();
                eventBase.setAction(Constants.SEND_MNAD_INFO_ACTION);
                eventBase.setData(status);
                EventBus.getDefault().post(eventBase);
            }
            else if(action.equals("UdiskInfo")) {
                EventBase eventBase = new EventBase();
                eventBase.setAction(Constants.SEND_UDISK_INFO_ACTION);
                eventBase.setData(status);
                EventBus.getDefault().post(eventBase);
            }
        }
    };

}
