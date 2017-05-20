
package com.actions.actcommunication;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import static android.R.attr.data;

/**
 * 通讯模块接口 <br>
 * Created by hexibin on 2016/5/23.
 */
public class ActCommunication {
    /**
     * 图片文件类型
     */
    public static final int FILE_TYPE_PHOTO = 1;
    /**
     * 视频文件类型
     */
    public static final int FILE_TYPE_VIDEO = 2;
    /**
     * 音乐文件类型
     */
    public static final int FILE_TYPE_MUSIC = 3;
    /**
     * 所有文件类型
     */
    public static final int FILE_TYPE_ALL = 4;

    private static final int AC_RECV_MSG = 1;
    private static final int AC_RECV_DATA = 2;
    private static final int AC_STATUS_CHANGE = 3;

    /**
     * 最大音量
     */
    public static final int MAX_VOLUME = 40;
    /**
     * 最小音量
     */
    public static final int MIN_VOLUME = 0;

    /**
     * 循环模式：顺序
     */
    public static final int LOOP_TYPE_SEQUENCE = 0;
    /**
     * 循环模式：循环所有文件
     */
    public static final int LOOP_TYPE_LOOPALL = 1;
    /**
     * 循环模式：循环单个文件
     */
    public static final int LOOP_TYPE_LOOPONE = 2;
    /**
     * 循环模式：随机
     */
    public static final int LOOP_TYPE_RANDOM = 3;

    /**
     * 播放器状态：停止
     */
    public static final int PLAYER_STATUS_STOP = 0;
    /**
     * 播放器状态：正在播放
     */
    public static final int PLAYER_STATUS_PLAYING = 1;
    /**
     * 播放器状态：暂停
     */
    public static final int PLAYER_STATUS_PAUSE = 2;
    /**
     * 播放器状态：出错
     */
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

    static {
        try {
            System.loadLibrary("actcommunication");
        } catch (UnsatisfiedLinkError e) {
            Log.e(LOGTAG, "UnsatisfiedLinkError:" + e.toString());
        }
    }

    private static ActCommunication ac = new ActCommunication();
    private EventHandler mHandler;

    private ActCommunication() {
        nativeSetup(this);
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mHandler = new EventHandler(looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mHandler = new EventHandler(looper);
        } else {
            mHandler = null;
        }
    }

    /**
     * 获取通讯模块实例
     *
     * @return 返回通讯模块实例
     */
    public static ActCommunication getInstance() {
        return ac;
    }

    /**
     * 设置事件监听器
     *
     * @param listener 事件监听器
     */
    public void setEventListener(AcEventListener listener) {
        mEventListener = listener;
    }

    private AcEventListener mEventListener;

    private static void onRecvMsg(Object ref, String[] msg) {
        ActCommunication mPf = (ActCommunication) ref;
        if (mPf == null) {
            return;
        }

        try {
            Message m = mPf.mHandler.obtainMessage();

            m.what = AC_RECV_MSG;
            Bundle bundle = new Bundle();
            bundle.putStringArray("msg", (String[]) msg);
            m.setData(bundle);

            mPf.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int setBrightness(int volume) {
        String[] cmd = new String[]{"cmd", "setBrightness", "brightness", ""};
        cmd[3] = Integer.toString(volume);
        return this.sendMsg(cmd);
    }

    public int requestBrightness() {
        String[] cmd = new String[]{"cmd", "reqBrightness"};
        return this.sendMsg(cmd);
    }

    private static void onRecvData(Object ref, String[] msg, byte[] data) {
        ActCommunication mPf = (ActCommunication) ref;
        if (mPf == null) {
            return;
        }

        try {
            Message m = mPf.mHandler.obtainMessage();

            m.what = AC_RECV_DATA;
            Bundle bundle = new Bundle();
            bundle.putStringArray("msg", (String[]) msg);
            bundle.putByteArray("data", data);
            m.setData(bundle);

            mPf.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void onStatusChange(Object ref, String status) {
        ActCommunication mPf = (ActCommunication) ref;
        if (mPf == null) {
            return;
        }

        try {
            Message m = mPf.mHandler.obtainMessage();

            m.what = AC_STATUS_CHANGE;
            Bundle bundle = new Bundle();
            bundle.putString("status", status);
            m.setData(bundle);

            mPf.mHandler.sendMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // handle
    private class EventHandler extends Handler {

        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (mEventListener == null)
                return;

            Bundle bundle = msg.getData();
            switch (msg.what) {
                case AC_RECV_MSG: {
                    String[] msgs = bundle.getStringArray("msg");
                    try {
                        if (msgs[2].equals("reply")) {
                            String state = msgs[1];
                            ActCommunication.this.mEventListener.ThumbnailReady(msgs[3], "ThumbnaiFailed".equals(state));
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!msgs[0].equals(MSG_KEY_CMD) || !msgs[1].equals(MSG_CMD_UPDATE_STATUS))
                        break;

                    for (int i = 2; i < msgs.length; i += 2) {
                        if (msgs[i].equals(MSG_KEY_VOLUME)) {
                            mEventListener.onRecvVolume(Integer.parseInt(msgs[i + 1]));
                        } else if (msgs[i].equals(MSG_KEY_SEQUENCE)) {
                            mEventListener.onRecvPlaySequence(Integer.parseInt(msgs[i + 1]));
                        } else if (msgs[i].equals(MSG_KEY_TOTAL_TIME)) {
                            mEventListener.onRecvTotalTime(Integer.parseInt(msgs[i + 1]));
                        } else if (msgs[i].equals(MSG_KEY_CURRENT_TIME)) {
                            mEventListener.onRecvCurrentTime(Integer.parseInt(msgs[i + 1]));
                        } else if (msgs[i].equals(MSG_KEY_PLAYER_STATUS)) {
                            mEventListener.onRecvPlayerStatus(Integer.parseInt(msgs[i + 1]));
                        } else if (msgs[i].equalsIgnoreCase("NandInfo") || msgs[i].equalsIgnoreCase("UdiskInfo")
                                || msgs[i].equalsIgnoreCase("brightness") || msgs[i].equalsIgnoreCase("storageInfo")) {
                            ActCommunication.this.mEventListener.onRecvResult(msgs[i], msgs);
                        }
                    }
                    break;
                }
                case AC_RECV_DATA: {
                    String[] msgs = bundle.getStringArray("msg");
                    byte[] data = bundle.getByteArray("data");
                    if (!msgs[0].equals(MSG_KEY_CMD) || !msgs[1].equals(MSG_CMD_UPDATE_THUMBNAIL))
                        break;

                    for (int i = 2; i < msgs.length; i += 2) {
                        if (msgs[i].equals(MSG_KEY_URL)) {
                            mEventListener.onRecvThumbnail(msgs[i + 1], data);
                        }
                    }
                    break;
                }

                case AC_STATUS_CHANGE: {
                    String status = bundle.getString("status");
                    if (status.equals("connect"))
                        mEventListener.onDeviceConnected();
                    else
                        mEventListener.onDeviceDisconnect();
                    break;
                }

                default:
                    break;
            }
        }
    }

    /**
     * 设置音量，音量取值范围：0~40
     *
     * @param volume 要设置的音量值
     * @return 成功返回0，失败返回-1
     */
    public int setVolume(int volume) {
        String[] cmd = new String[]{"cmd", "setVolume", "volume", ""};
        cmd[3] = Integer.toString(volume);
        return this.sendMsg(cmd);
    }

    /**
     * 请求设备当前音量，音量值通过EventListener的onRecvVolume事件返回
     *
     * @return 成功返回0，失败返回-1
     */
    public int requestVolume() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_REQUEST_VOLUME;
        return sendMsg(cmd);
    }

    /**
     * 播放文件
     *
     * @param url      要播放的文件在相框中的路径
     * @param fileType 文件类型，FILE_TYPE_PHOTO, FILE_TYPE_MUSIC, FILE_TYPE_VIDEO
     * @return 成功返回0，失败返回-1
     */
    public int playFile(String url, int fileType) {
        String[] cmd = new String[6];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_PLAY_FILE;
        cmd[2] = MSG_KEY_URL;
        cmd[3] = url;
        cmd[4] = MSG_KEY_FILE_TYPE;
        cmd[5] = Integer.toString(fileType);
        return sendMsg(cmd);
    }

    /**
     * 暂停播放
     *
     * @return 成功返回0，失败返回-1
     */
    public int pause() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_PAUSE;
        return sendMsg(cmd);
    }

    /**
     * 恢复播放
     *
     * @return 成功返回0，失败返回-1
     */
    public int resume() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_RESUME;
        return sendMsg(cmd);
    }

    /**
     * 播放器seek
     *
     * @param timeMs 要seek到的时间点，单位毫秒
     * @return 成功返回0，失败返回-1
     */
    public int seek(int timeMs) {
        String[] cmd = new String[4];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_SEEK;
        cmd[3] = MSG_KEY_TIME;
        cmd[4] = Integer.toString(timeMs);
        return sendMsg(cmd);
    }

    /**
     * 停止播放
     *
     * @return 成功返回0，失败返回-1
     */
    public int stop() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_STOP;
        return sendMsg(cmd);
    }

    /**
     * 播放上一个文件
     *
     * @return 成功返回0，失败返回-1
     */
    public int prevFile() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_PREV;
        return sendMsg(cmd);
    }

    /**
     * 播放下一个文件
     *
     * @return 成功返回0，失败返回-1
     */
    public int nextFile() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_NEXT;
        return sendMsg(cmd);
    }

    /**
     * 发送up消息
     *
     * @return 成功返回0，失败返回-1
     */
    public int sendKeyUp() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_UP;
        return sendMsg(cmd);
    }

    /**
     * 发送down消息
     *
     * @return 成功返回0，失败返回-1
     */
    public int sendKeyDown() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_DOWN;
        return sendMsg(cmd);
    }

    /**
     * 发送ok消息
     *
     * @return 成功返回0，失败返回-1
     */
    public int sendKeyOk() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_OK;
        return sendMsg(cmd);
    }

    /**
     * 发送return消息
     *
     * @return 成功返回0，失败返回-1
     */
    public int sendKeyReturn() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_RETURN;
        return sendMsg(cmd);
    }

    /**
     * 关机
     *
     * @return 成功返回0，失败返回-1
     */
    public int powerOff() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_POWEROFF;
        return sendMsg(cmd);
    }

    /**
     * 请求设备当前播放文件的总时间，通过EventListener的onRecvTotalTime事件返回
     *
     * @return 成功返回0，失败返回-1
     */
    public int requestTotalTime() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_TOTAL_TIME;
        return sendMsg(cmd);
    }

    /**
     * 请求设备当前播放进度，通过EventListener的onRecvCurrentTime事件返回
     *
     * @return 成功返回0，失败返回-1
     */
    public int requestCurrentTime() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_CURRENT_TIME;
        return sendMsg(cmd);
    }

    /**
     * 请求设备当前播放状态，通过EventListener的onRecvPlayerStatus事件返回
     *
     * @return 成功返回0，失败返回-1
     */
    public int requestPlayerStatus() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_PLAYER_STATUS;
        return sendMsg(cmd);
    }

    /**
     * 设置播放循环模式
     *
     * @param seq 取值 LOOP_TYPE_SEQUENCE、LOOP_TYPE_LOOPALL、LOOP_TYPE_LOOPONE、LOOP_TYPE_RANDOM
     * @return 成功返回0，失败返回-1
     */
    public int setPlaySequence(int seq) {
        String[] cmd = new String[4];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_SET_SEQUENCE;
        cmd[2] = MSG_KEY_SEQUENCE;
        cmd[3] = Integer.toString(seq);
        return sendMsg(cmd);
    }

    /**
     * 请求设备当前播放顺序，通过EventListener的onRecvPlaySequence事件返回
     *
     * @return 成功返回0，失败返回-1
     */
    public int requestPlaySequence() {
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_SEQUENCE;
        return sendMsg(cmd);
    }

    /**
     * 请求图片的缩略图，缩略图收到后通过EventListener返回
     *
     * @param urls 图片url数组
     * @return 成功返回0，失败返回-1
     */
    public int requestThumbnails(String[] urls) {
        String[] cmd = new String[2];
        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_THUMBNAIL;

        String data = "";
        for (int i = 0; i < urls.length; i++) {
            data += urls[i] + "\r\n";
        }
        return sendData(cmd, data.getBytes());
    }

    public int requestThumbnails(String url) {
        String[] cmd = new String[]{"cmd", "getThumbnail", "url", url};
        return sendMsg(cmd);
    }

    /**
     * 取消之前请求的缩略图，以便加快新的缩略图的获取
     *
     * @param urls 图片url数组
     * @return 成功返回0，失败返回-1
     */
    public int cancelThumbnails(String[] urls) {
        String[] cmd = new String[2];
        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_CANCEL_THUMBNAIL;

        String data = "";
        for (int i = 0; i < urls.length; i++) {
            data += urls[i] + "\r\n";
        }
        return sendData(cmd, data.getBytes());
    }

    /**
     * 已经上传文件，通知相框端更新播放列表
     *
     * @param url 文件在相框中的路径
     * @return 成功返回0，失败返回-1
     */
    public int onUploadFile(String url) {
        String[] cmd = new String[4];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_ADD_FILE;
        cmd[2] = MSG_KEY_URL;
        cmd[3] = url;
        return sendMsg(cmd);
    }

    /**
     * 已经删除文件，通知相框端更新播放列表
     *
     * @param url 文件在相框中的路径
     * @return 成功返回0，失败返回-1
     */
    public int onDeleteFile(String url) {
        String[] cmd = new String[4];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_DELETE_FILE;
        cmd[2] = MSG_KEY_URL;
        cmd[3] = url;
        return sendMsg(cmd);
    }

    private static native void nativeSetup(Object cc);

    /**
     * 连接服务器的消息通讯服务，连接成功后才能发送消息，连接成功或失败通过EventListener反馈
     *
     * @param ip 要连接的服务器的ip地址
     * @return 成功返回0，失败返回-1
     */
    public native int connect(String ip);

    /**
     * 断开与服务器的连接，断开后，无法发送消息到服务器。用于退出时释放资源
     *
     * @return 成功返回0，失败返回-1
     */
    public native int disconnect();

    /**
     * 向设备端发送消息，
     *
     * @param cmd 消息字符串数组，以[key, value, key, value....]的格式排放
     * @return 成功返回0，失败返回-1
     */
    public native int sendMsg(Object[] cmd);

    /**
     * 向设备端发送消息和二进制数据，
     *
     * @param cmd  消息字符串数组，以[key, value, key, value....]的格式排放
     * @param data 二进制数据数组
     * @return 成功返回0，失败返回-1
     */
    public native int sendData(Object[] cmd, byte[] data);

    public native int readSystemCfgFile();
}