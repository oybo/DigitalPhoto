
package com.actions.actfilemanager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

/**
 * ActFileManager FTP simple file manager
 */
public class ActFileManager {
    public static final int OP_TYPE_OPERATION_PROGRESSION = 0;
    public static final int OP_TYPE_UPLOAD = 1;
    public static final int OP_TYPE_DOWNLOAD = 2;
    public static final int OP_TYPE_DELETE = 3;
    public static final int OP_TYPE_BROWSE = 4;
    public static final int OP_TYPE_DELETE_DIR = 5;
    public static final int OP_TYPE_CREATE_DIR = 6;
    public static final int OP_TYPE_QUIT = 7;
    public static final int OP_TYPE_RENAME = 8;

    private static final String LOGTAG = "actfilemanager";

    static {
        try {
            System.loadLibrary("actfilemanager");
        } catch (UnsatisfiedLinkError e) {
            Log.e(LOGTAG, "UnsatisfiedLinkError:" + e.toString());
        }
    }

    //private static ActFileManager pf = new ActFileManager();
    private EventHandler mHandler = null;

    public ActFileManager() {
        nativeSetup(this);
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mHandler = new EventHandler(this, looper);
        } else {
            mHandler = null;
        }
    }

    private ACTFileEventListener mEventListener = null;

    public void registerEventListener(ACTFileEventListener listener) {
        mEventListener = listener;
    }

    public void clearEventListener() {
        mEventListener = null;
    }

    private synchronized void postOperationProgress(Object pfref, int opcode, int processed, int total) {
        ActFileManager mp = (ActFileManager) pfref;
        if (mp == null) {
            return;
        }
        //Log.d(LOGTAG, "opcode = " + opcode + " processed = " + processed + " total = " + total);

        try {
            Message m = mp.mHandler.obtainMessage();
            m.what = OP_TYPE_OPERATION_PROGRESSION;
            Bundle bundle = new Bundle();
            bundle.putInt("opcode", opcode);
            bundle.putInt("processed", processed);
            bundle.putInt("total", total);
            m.setData(bundle);
            mp.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void postDisconnectResponse(Object pfref, int result) {
        ActFileManager mp = (ActFileManager) pfref;
        if (mp == null) {
            return;
        }
        Log.d(LOGTAG, "[JAVA] Disconnect result = " + result);
        try {
            Message m = mp.mHandler.obtainMessage();
            m.what = OP_TYPE_QUIT;
            m.arg1 = result;
            mp.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void postUploadResponse(Object pfref, String remotePath, String filePath, int result) {
        ActFileManager mp = (ActFileManager) pfref;
        if (mp == null) {
            return;
        }
        Log.d(LOGTAG, "[JAVA] " + filePath + " upload to " + remotePath + " result = " + result);

        try {
            Message m = mp.mHandler.obtainMessage();
            m.what = OP_TYPE_UPLOAD;
            Bundle bundle = new Bundle();
            bundle.putString("remote_path", remotePath);
            bundle.putString("file_path", filePath);
            if (result == 0)
                bundle.putInt("result", ACTFileEventListener.OPERATION_SUCESSFULLY);
            else
                bundle.putInt("result", ACTFileEventListener.OPERATION_FAILED);
            m.setData(bundle);
            mp.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void postDeleteResponse(Object pfref, String filePath, int result) {
        ActFileManager mp = (ActFileManager) pfref;
        if (mp == null) {
            return;
        }
        Log.d(LOGTAG, "<JAVA> delete " + filePath + " result = " + result);

        try {
            Message m = mp.mHandler.obtainMessage();
            m.what = OP_TYPE_DELETE;
            Bundle bundle = new Bundle();
            bundle.putString("file_path", filePath);
            if (result == 0)
                bundle.putInt("result", ACTFileEventListener.OPERATION_SUCESSFULLY);
            else
                bundle.putInt("result", ACTFileEventListener.OPERATION_FAILED);
            m.setData(bundle);
            mp.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void postDeleteDirectoryResponse(Object pfref, String parentPath, int result) {
        ActFileManager mp = (ActFileManager) pfref;
        if (mp == null) {
            return;
        }
        Log.d(LOGTAG, "<JAVA> delete direcotory under " + parentPath + " result = " + result);

        try {
            Message m = mp.mHandler.obtainMessage();
            m.what = OP_TYPE_DELETE_DIR;
            Bundle bundle = new Bundle();
            bundle.putString("parent_path", parentPath);
            if (result == 0)
                bundle.putInt("result", ACTFileEventListener.OPERATION_SUCESSFULLY);
            else
                bundle.putInt("result", ACTFileEventListener.OPERATION_FAILED);
            m.setData(bundle);
            mp.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void postCreateDirectoryResponse(Object pfref, String parentPath, int result) {
        ActFileManager mp = (ActFileManager) pfref;
        if (mp == null) {
            return;
        }
        Log.d(LOGTAG, "<JAVA> create direcotory under " + parentPath + " result = " + result);

        try {
            Message m = mp.mHandler.obtainMessage();
            m.what = OP_TYPE_CREATE_DIR;
            Bundle bundle = new Bundle();
            bundle.putString("parent_path", parentPath);
            if (result == 0)
                bundle.putInt("result", ACTFileEventListener.OPERATION_SUCESSFULLY);
            else
                bundle.putInt("result", ACTFileEventListener.OPERATION_FAILED);
            m.setData(bundle);
            mp.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void postBrowseResponse(Object pfref, Object filelist, String currentPath, int result) {
        ActFileManager mp = (ActFileManager) pfref;
        if (mp == null) {
            Log.e(LOGTAG, "ActFileManager is null");
            return;
        }
        Log.d(LOGTAG, "<JAVA> postBrowseResponse on " + currentPath + " result = " + result);

        try {
            Message m = mp.mHandler.obtainMessage();
            //>>>>>JUST FOR DEBUG<<<<<<
            //Log.d(LOGTAG, "BrowseResponse result:");
            //for (int i = 0; i < ((ArrayList) filelist).size(); i++) {
            //   ActFileInfo info = (ActFileInfo) ((ArrayList) filelist).get(i);
            //    Log.d(LOGTAG, "<" + i + ">:" + info.getFileName());
            //}
            //>>>>>JUST FOR DEBUG<<<<<<
            m.what = OP_TYPE_BROWSE;
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("file_list", (ArrayList) filelist);
            bundle.putString("current_path",currentPath);
            if (result == 0)
                bundle.putInt("result", ACTFileEventListener.OPERATION_SUCESSFULLY);
            else
                bundle.putInt("result", ACTFileEventListener.OPERATION_FAILED);
            m.setData(bundle);
            mp.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void postDownloadResponse(Object pfref, String remotePath, String filePath, int result) {
        ActFileManager mp = (ActFileManager) pfref;
        if (mp == null) {
            return;
        }
        Log.d(LOGTAG, remotePath + " to " + filePath + " result " + result);
        try {
            Message m = mp.mHandler.obtainMessage();
            m.what = OP_TYPE_DOWNLOAD;
            Bundle bundle = new Bundle();
            bundle.putString("remote_path", remotePath);
            bundle.putString("file_path", filePath);
            if (result == 0)
                bundle.putInt("result", ACTFileEventListener.OPERATION_SUCESSFULLY);
            else
                bundle.putInt("result", ACTFileEventListener.OPERATION_FAILED);
            m.setData(bundle);
            mp.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void postRenameResponse(Object pfref, int result) {
        ActFileManager mp = (ActFileManager) pfref;
        if (mp == null) {
            return;
        }
        Log.d(LOGTAG, "<JAVA> rename result = " + result);

        try {
            Message m = mp.mHandler.obtainMessage();
            m.what = OP_TYPE_RENAME;
            Bundle bundle = new Bundle();
            if (result == 0)
                bundle.putInt("result", ACTFileEventListener.OPERATION_SUCESSFULLY);
            else
                bundle.putInt("result", ACTFileEventListener.OPERATION_FAILED);
            m.setData(bundle);
            mp.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class EventHandler extends Handler {
        private ActFileManager mPf;

        public EventHandler(ActFileManager pf, Looper looper) {
            super(looper);
            mPf = pf;
        }

        public void handleMessage(Message msg) {
            if (mEventListener == null)
                return;
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case OP_TYPE_OPERATION_PROGRESSION: {
                    int opcode = bundle.getInt("opcode");
                    int processed = bundle.getInt("processed");
                    int total = bundle.getInt("total");
                    mEventListener.onOperationProgression(opcode, processed, total);
                    break;
                }
                case OP_TYPE_UPLOAD: {
                    String remotePath = bundle.getString("remote_path");
                    String filePath = bundle.getString("file_path");
                    int result = bundle.getInt("result");
                    mEventListener.onUploadCompleted(remotePath, filePath, result);
                    break;
                }
                case OP_TYPE_DOWNLOAD: {
                    String remotePath = bundle.getString("remote_path");
                    String filePath = bundle.getString("file_path");
                    int result = bundle.getInt("result");
                    mEventListener.onDownloadCompleted(remotePath, filePath, result);
                    break;
                }
                case OP_TYPE_DELETE: {
                    String parentPath = bundle.getString("parent_path");
                    int result = bundle.getInt("result");
                    mEventListener.onDeleteCompleted(parentPath, result);
                    break;
                }
                case OP_TYPE_BROWSE: {
                    ArrayList filelist = bundle.getParcelableArrayList("file_list");
                    int result = bundle.getInt("result");
                    String currentPath = bundle.getString("current_path");
                    mEventListener.onBrowseCompleted(filelist,currentPath,result);
                    break;
                }
                case OP_TYPE_DELETE_DIR: {
                    String parentPath = bundle.getString("parent_path");
                    int result = bundle.getInt("result");
                    mEventListener.onDeleteDirectoryCompleted(parentPath, result);
                    break;
                }

                case OP_TYPE_CREATE_DIR: {
                    String parentPath = bundle.getString("parent_path");
                    int result = bundle.getInt("result");
                    mEventListener.onCreateDirectoryCompleted(parentPath, result);
                    break;
                }
                case OP_TYPE_QUIT: {
                    int result = msg.arg1;
                    mEventListener.onDisconnectCompleted(result);
                    break;
                }
                case OP_TYPE_RENAME: {
                    int result = bundle.getInt("result");
                    mEventListener.onRenameCompleted(result);
                    break;
                }
                default:
                    break;
            }
        }
    }

    private static native void nativeSetup(Object cc);

    private static native void nativeClose();

    /**
     * connect to the target remote server
     *
     * @param ip
     * @return if 0 means call successfully
     */
    public static native int connect(String ip);

    /**
     * disconnect with the remote
     *
     * @return if 0 means call successfully
     */
    public static native int disconnect();

    /**
     * upload t he file to the remote
     *
     * @param LocalFile  the local file path,like "/sdcard/helloworld.tar"
     * @param remotePath the target remote path,like "/hello/helloworld.tar",remember should add the file name into the path
     * @return if 0 means call successfully
     */
    public static native int uploadFile(String LocalFile, String remotePath);

    /**
     * delete the file on the remote path
     *
     * @param filePath the target file path,like "/hello/helloworld.tar",helloworld.tar is which try to delete
     * @return if 0 means call successfully
     */
    public static native int deleteFile(String filePath);

    /**
     * delete the directory for the remote path
     *
     * @param remotePath the target remote path.like "/hello/helloworld/",remember end with "/" and the helloworld will be the directory name try to delete
     * @return if 0 means call successfully
     */
    public static native int deleteDirectory(String remotePath);

    /**
     * create the directory under the target remote path
     *
     * @param remotePath the target remote path,like "/hello/helloworld/",remember end with "/" and the helloworld will be the directory name try to create
     * @return if 0 means call successfully
     */
    public static native int createDirectory(String remotePath);

    /**
     * browse the remote directory
     *
     * @param remotePath the path on the remote,like "/hello/",remember end with "/" to indicate it is a directory
     * @return if 0 means call successfully
     */
    public static native int browseFiles(String remotePath);

    /**
     * download the file from the remote
     *
     * @param remotePath the file path on the remote,like "/hello/helloworld.tar"
     * @param localPath  the local path to store the file,like "/sdcard/helloworld.tar"
     * @return if 0 means call successfully
     */
    public static native int downloadFile(String remotePath, String localPath);

    /**
     * rename file or move file to other directory
     *
     * @param from the file path to rename,like "/1/helloworld.tar"
     * @param to  the file path rename to,like "/2/hi.tar"
     * @return if 0 means call successfully
     */
    public static native int rename(String from, String to);
}