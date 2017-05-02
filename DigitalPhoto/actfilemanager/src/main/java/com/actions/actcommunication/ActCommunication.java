//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.actions.actcommunication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ActCommunication {
    public static final int FILE_TYPE_PHOTO = 1;
    public static final int FILE_TYPE_VIDEO = 2;
    public static final int FILE_TYPE_MUSIC = 3;
    public static final int FILE_TYPE_ALL = 4;
    private static final int AC_RECV_MSG = 1;
    private static final int AC_RECV_DATA = 2;
    private static final int AC_STATUS_CHANGE = 3;
    public static final int MAX_VOLUME = 40;
    public static final int MIN_VOLUME = 0;
    public static final int LOOP_TYPE_SEQUENCE = 0;
    public static final int LOOP_TYPE_LOOPALL = 1;
    public static final int LOOP_TYPE_LOOPONE = 2;
    public static final int LOOP_TYPE_RANDOM = 3;
    public static final int PLAYER_STATUS_STOP = 0;
    public static final int PLAYER_STATUS_PLAYING = 1;
    public static final int PLAYER_STATUS_PAUSE = 2;
    public static final int PLAYER_STATUS_ERROR = 3;
    private static final String MSG_KEY_CMD = "cmd";
    private static final String MSG_KEY_VOLUME = "volume";
    private static final String MSG_KEY_URL = "url";
    private static final String MSG_KEY_FILE_TYPE = "fileType";
    private static final String MSG_KEY_SEQUENCE = "sequence";
    private static final String MSG_KEY_TOTAL_TIME = "totalTime";
    private static final String MSG_KEY_CURRENT_TIME = "currentTime";
    private static final String MSG_KEY_PLAYER_STATUS = "playerStatus";
    private static final String MSG_KEY_TIME = "time";
    private static final String MSG_CMD_SET_VOLUME = "setVolume";
    private static final String MSG_CMD_REQUEST_VOLUME = "requestVolume";
    private static final String MSG_CMD_PLAY_FILE = "playFile";
    private static final String MSG_CMD_PAUSE = "pause";
    private static final String MSG_CMD_RESUME = "resume";
    private static final String MSG_CMD_SEEK = "seek";
    private static final String MSG_CMD_STOP = "stop";
    private static final String MSG_CMD_PREV = "prev";
    private static final String MSG_CMD_NEXT = "next";
    private static final String MSG_CMD_UP = "up";
    private static final String MSG_CMD_DOWN = "down";
    private static final String MSG_CMD_OK = "ok";
    private static final String MSG_CMD_RETURN = "return";
    private static final String MSG_CMD_POWEROFF = "powerOff";
    private static final String MSG_CMD_GET_TOTAL_TIME = "getTotalTime";
    private static final String MSG_CMD_GET_CURRENT_TIME = "getCurrentTime";
    private static final String MSG_CMD_GET_PLAYER_STATUS = "getStatus";
    private static final String MSG_CMD_SET_SEQUENCE = "setSequence";
    private static final String MSG_CMD_GET_SEQUENCE = "getSequence";
    private static final String MSG_CMD_UPDATE_STATUS = "update";
    private static final String MSG_CMD_GET_THUMBNAIL = "getThumbnail";
    private static final String MSG_CMD_CANCEL_THUMBNAIL = "cancelThumbnail";
    private static final String MSG_CMD_UPDATE_THUMBNAIL = "updateThumbnail";
    private static final String MSG_CMD_ADD_FILE = "addFile";
    private static final String MSG_CMD_DELETE_FILE = "deleteFile";
    private static final String LOGTAG = "ActCommunication";
    private static ActCommunication ac;
    private EventHandler mHandler;
    private AcEventListener mEventListener;

    private ActCommunication() {
        nativeSetup(this);
        Looper looper;
        if((looper = Looper.myLooper()) != null) {
            this.mHandler = new EventHandler(looper);
        } else if((looper = Looper.getMainLooper()) != null) {
            this.mHandler = new EventHandler(looper);
        } else {
            this.mHandler = null;
        }

    }

    public static ActCommunication getInstance() {
        return ac;
    }

    public void setEventListener(AcEventListener listener) {
        this.mEventListener = listener;
    }

    private static void onRecvMsg(Object ref, String[] msg) {
        ActCommunication mPf = (ActCommunication)ref;
        if(mPf != null) {
            try {
                Message e = mPf.mHandler.obtainMessage();
                e.what = 1;
                Bundle bundle = new Bundle();
                bundle.putStringArray("msg", (String[])msg);
                e.setData(bundle);
                mPf.mHandler.sendMessage(e);
            } catch (Exception var5) {
                var5.printStackTrace();
            }

        }
    }

    private static void onRecvData(Object ref, String[] msg, byte[] data) {
        ActCommunication mPf = (ActCommunication)ref;
        if(mPf != null) {
            try {
                Message e = mPf.mHandler.obtainMessage();
                e.what = 2;
                Bundle bundle = new Bundle();
                bundle.putStringArray("msg", (String[])msg);
                bundle.putByteArray("data", data);
                e.setData(bundle);
                mPf.mHandler.sendMessage(e);
            } catch (Exception var6) {
                var6.printStackTrace();
            }

        }
    }

    private static void onStatusChange(Object ref, String status) {
        ActCommunication mPf = (ActCommunication)ref;
        if(mPf != null) {
            try {
                Message e = mPf.mHandler.obtainMessage();
                e.what = 3;
                Bundle bundle = new Bundle();
                bundle.putString("status", status);
                e.setData(bundle);
                mPf.mHandler.sendMessage(e);
            } catch (Exception var5) {
                var5.printStackTrace();
            }

        }
    }

    public int setVolume(int volume) {
        String[] cmd = new String[]{"cmd", "setVolume", "volume", ""};
        cmd[3] = Integer.toString(volume);
        return this.sendMsg(cmd);
    }

    public int setBrightness(int volume) {
        String[] cmd = new String[]{"cmd", "setBrightness", "brightness", ""};
        cmd[3] = Integer.toString(volume);
        return this.sendMsg(cmd);
    }

    public int requestVolume() {
        String[] cmd = new String[]{"cmd", "requestVolume"};
        return this.sendMsg(cmd);
    }

    public int requestBrightness() {
        String[] cmd = new String[]{"cmd", "reqBrightness"};
        return this.sendMsg(cmd);
    }

    public int playFile(String url, int fileType) {
        String[] cmd = new String[]{"cmd", "playFile", "url", url, "fileType", Integer.toString(fileType)};
        return this.sendMsg(cmd);
    }

    public int pause() {
        String[] cmd = new String[]{"cmd", "pause"};
        return this.sendMsg(cmd);
    }

    public int resume() {
        String[] cmd = new String[]{"cmd", "resume"};
        return this.sendMsg(cmd);
    }

    public int seek(int timeMs) {
        String[] cmd = new String[]{"cmd", "seek", null, "time"};
        cmd[4] = Integer.toString(timeMs);
        return this.sendMsg(cmd);
    }

    public int stop() {
        String[] cmd = new String[]{"cmd", "stop"};
        return this.sendMsg(cmd);
    }

    public int prevFile() {
        String[] cmd = new String[]{"cmd", "prev"};
        return this.sendMsg(cmd);
    }

    public int nextFile() {
        String[] cmd = new String[]{"cmd", "next"};
        return this.sendMsg(cmd);
    }

    public int sendKeyUp() {
        String[] cmd = new String[]{"cmd", "up"};
        return this.sendMsg(cmd);
    }

    public int sendKeyDown() {
        String[] cmd = new String[]{"cmd", "down"};
        return this.sendMsg(cmd);
    }

    public int sendKeyOk() {
        String[] cmd = new String[]{"cmd", "ok"};
        return this.sendMsg(cmd);
    }

    public int sendKeyReturn() {
        String[] cmd = new String[]{"cmd", "return"};
        return this.sendMsg(cmd);
    }

    public int powerOff() {
        String[] cmd = new String[]{"cmd", "powerOff"};
        return this.sendMsg(cmd);
    }

    public int requestTotalTime() {
        String[] cmd = new String[]{"cmd", "getTotalTime"};
        return this.sendMsg(cmd);
    }

    public int requestCurrentTime() {
        String[] cmd = new String[]{"cmd", "getCurrentTime"};
        return this.sendMsg(cmd);
    }

    public int requestPlayerStatus() {
        String[] cmd = new String[]{"cmd", "getStatus"};
        return this.sendMsg(cmd);
    }

    public int setPlaySequence(int seq) {
        String[] cmd = new String[]{"cmd", "setSequence", "sequence", Integer.toString(seq)};
        return this.sendMsg(cmd);
    }

    public int requestPlaySequence() {
        String[] cmd = new String[]{"cmd", "getSequence"};
        return this.sendMsg(cmd);
    }

    public int requestThumbnails(String[] urls) {
        String[] cmd = new String[]{"cmd", "getThumbnail"};
        String data = "";

        for(int i = 0; i < urls.length; ++i) {
            data = data + urls[i] + "\r\n";
        }

        return this.sendData(cmd, data.getBytes());
    }

    public int cancelThumbnails(String[] urls) {
        String[] cmd = new String[]{"cmd", "cancelThumbnail"};
        String data = "";

        for(int i = 0; i < urls.length; ++i) {
            data = data + urls[i] + "\r\n";
        }

        return this.sendData(cmd, data.getBytes());
    }

    public int onUploadFile(String url) {
        String[] cmd = new String[]{"cmd", "addFile", "url", url};
        return this.sendMsg(cmd);
    }

    public int onDeleteFile(String url) {
        String[] cmd = new String[]{"cmd", "deleteFile", "url", url};
        return this.sendMsg(cmd);
    }

    private static native void nativeSetup(Object var0);

    public native int connect(String var1);

    public native int disconnect();

    public native int sendMsg(Object[] var1);

    public native int sendData(Object[] var1, byte[] var2);

    static {
        try {
            System.loadLibrary("actcommunication");
        } catch (UnsatisfiedLinkError var1) {
            Log.e("ActCommunication", "UnsatisfiedLinkError:" + var1.toString());
        }

        ac = new ActCommunication();
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if(ActCommunication.this.mEventListener != null) {
                Bundle bundle = msg.getData();
                String[] status1;
                switch(msg.what) {
                    case 1:
                        status1 = bundle.getStringArray("msg");
                        if(status1[0].equals("cmd") && status1[1].equals("update")) {
                            for(int data1 = 2; data1 < status1.length; data1 += 2) {
                                String state = status1[data1];
                                if(state.equals("volume")) {
                                    ActCommunication.this.mEventListener.onRecvVolume(Integer.parseInt(status1[data1 + 1]));
                                } else if(state.equals("sequence")) {
                                    ActCommunication.this.mEventListener.onRecvPlaySequence(Integer.parseInt(status1[data1 + 1]));
                                } else if(state.equals("totalTime")) {
                                    ActCommunication.this.mEventListener.onRecvTotalTime(Integer.parseInt(status1[data1 + 1]));
                                } else if(state.equals("currentTime")) {
                                    ActCommunication.this.mEventListener.onRecvCurrentTime(Integer.parseInt(status1[data1 + 1]));
                                } else if(state.equals("playerStatus")) {
                                    ActCommunication.this.mEventListener.onRecvPlayerStatus(Integer.parseInt(status1[data1 + 1]));
                                } else if(state.equalsIgnoreCase("NandInfo") || state.equalsIgnoreCase("UdiskInfo")) {
                                    ActCommunication.this.mEventListener.onRecvResult(status1[data1], status1);
                                }
                            }
                        }
                        break;
                    case 2:
                        status1 = bundle.getStringArray("msg");
                        byte[] data = bundle.getByteArray("data");
                        if(status1[0].equals("cmd") && status1[1].equals("updateThumbnail")) {
                            for(int i = 2; i < status1.length; i += 2) {
                                if(status1[i].equals("url")) {
                                    ActCommunication.this.mEventListener.onRecvThumbnail(status1[i + 1], data);
                                }
                            }
                        }
                        break;
                    case 3:
                        String status = bundle.getString("status");
                        if(status.equals("connect")) {
                            ActCommunication.this.mEventListener.onDeviceConnected();
                        } else {
                            ActCommunication.this.mEventListener.onDeviceDisconnect();
                        }
                }

            }
        }
    }
}
