
package com.actions.actcommunication;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * ͨѶģ��ӿ� <br>
 * Created by hexibin on 2016/5/23.
 */
public class ActCommunication
{
    /** ͼƬ�ļ����� */
    public static final int FILE_TYPE_PHOTO = 1;
    /** ��Ƶ�ļ����� */
    public static final int FILE_TYPE_VIDEO = 2;
    /** �����ļ����� */
    public static final int FILE_TYPE_MUSIC = 3;
    /** �����ļ����� */
    public static final int FILE_TYPE_ALL = 4;

    private static final int AC_RECV_MSG = 1;
    private static final int AC_RECV_DATA = 2;
    private static final int AC_STATUS_CHANGE = 3;

    /** ������� */
    public static final int MAX_VOLUME = 40;
    /** ��С���� */
    public static final int MIN_VOLUME = 0;

    /** ѭ��ģʽ��˳�� */
    public static final int LOOP_TYPE_SEQUENCE = 0;
    /** ѭ��ģʽ��ѭ�������ļ� */
    public static final int LOOP_TYPE_LOOPALL = 1;
    /** ѭ��ģʽ��ѭ�������ļ� */
    public static final int LOOP_TYPE_LOOPONE = 2;
    /** ѭ��ģʽ����� */
    public static final int LOOP_TYPE_RANDOM = 3;

    /** ������״̬��ֹͣ */
    public static final int PLAYER_STATUS_STOP = 0;
    /** ������״̬�����ڲ��� */
    public static final int PLAYER_STATUS_PLAYING = 1;
    /** ������״̬����ͣ */
    public static final int PLAYER_STATUS_PAUSE = 2;
    /** ������״̬������ */
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
    
    static
    {
        try{
    		System.loadLibrary("actcommunication");
    	}catch (UnsatisfiedLinkError e) {
    		Log.e(LOGTAG,"UnsatisfiedLinkError:"+e.toString());
		}
    }
    
    private static ActCommunication ac = new ActCommunication();
    private EventHandler mHandler;

    private ActCommunication(){
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
     * ��ȡͨѶģ��ʵ��
     * @return ����ͨѶģ��ʵ��
     */
    public static ActCommunication getInstance(){
        return ac;
    }

    /**
     * �����¼�������
     * @param listener �¼�������
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
            if(mEventListener == null)
                return;

            Bundle bundle = msg.getData();
            switch (msg.what) {
                case AC_RECV_MSG: {
                    String[] msgs = bundle.getStringArray("msg");
                    if(!msgs[0].equals(MSG_KEY_CMD) || !msgs[1].equals(MSG_CMD_UPDATE_STATUS))
                        break;

                    for(int i=2; i<msgs.length; i+=2){
                        if(msgs[i].equals(MSG_KEY_VOLUME)) {
                            mEventListener.onRecvVolume(Integer.parseInt(msgs[i + 1]));
                        } else if(msgs[i].equals(MSG_KEY_SEQUENCE)) {
                            mEventListener.onRecvPlaySequence(Integer.parseInt(msgs[i + 1]));
                        } else if(msgs[i].equals(MSG_KEY_TOTAL_TIME)) {
                            mEventListener.onRecvTotalTime(Integer.parseInt(msgs[i + 1]));
                        } else if(msgs[i].equals(MSG_KEY_CURRENT_TIME)) {
                            mEventListener.onRecvCurrentTime(Integer.parseInt(msgs[i + 1]));
                        } else if(msgs[i].equals(MSG_KEY_PLAYER_STATUS)) {
                            mEventListener.onRecvPlayerStatus(Integer.parseInt(msgs[i + 1]));
                        }
                    }
                    break;
                }
                case AC_RECV_DATA: {
                    String[] msgs = bundle.getStringArray("msg");
                    byte[] data = bundle.getByteArray("data");
                    if(!msgs[0].equals(MSG_KEY_CMD) || !msgs[1].equals(MSG_CMD_UPDATE_THUMBNAIL))
                        break;

                    for(int i=2; i<msgs.length; i+=2){
                        if(msgs[i].equals(MSG_KEY_URL)) {
                            mEventListener.onRecvThumbnail(msgs[i+1], data);
                        }
                    }
                    break;
                }

                case AC_STATUS_CHANGE: {
                    String status = bundle.getString("status");
                    if(status.equals("connect"))
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
    * ��������������ȡֵ��Χ��0~40
    * @param volume Ҫ���õ�����ֵ
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int setVolume(int volume){
        String[] cmd = new String[4];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_SET_VOLUME;
        cmd[3] = MSG_KEY_VOLUME;
        cmd[4] = Integer.toString(volume);
        return sendMsg(cmd);
    }

    /**
    * �����豸��ǰ����������ֵͨ��EventListener��onRecvVolume�¼�����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int requestVolume(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_REQUEST_VOLUME;
        return sendMsg(cmd);
    }

    /**
    * �����ļ�
    * @param url Ҫ���ŵ��ļ�������е�·��
    * @param fileType �ļ����ͣ�FILE_TYPE_PHOTO, FILE_TYPE_MUSIC, FILE_TYPE_VIDEO
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int playFile(String url, int fileType){
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
    * ��ͣ����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int pause(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_PAUSE;
        return sendMsg(cmd);
    }

    /**
    * �ָ�����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int resume(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_RESUME;
        return sendMsg(cmd);
    }

    /**
    * ������seek
    * @param timeMs Ҫseek����ʱ��㣬��λ����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int seek(int timeMs){
        String[] cmd = new String[4];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_SEEK;
        cmd[3] = MSG_KEY_TIME;
        cmd[4] = Integer.toString(timeMs);
        return sendMsg(cmd);
    }

    /**
    * ֹͣ����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int stop(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_STOP;
        return sendMsg(cmd);
    }

    /**
    * ������һ���ļ�
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int prevFile(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_PREV;
        return sendMsg(cmd);
    }

    /**
    * ������һ���ļ�
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int nextFile(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_NEXT;
        return sendMsg(cmd);
    }

    /**
    * ����up��Ϣ
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int sendKeyUp(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_UP;
        return sendMsg(cmd);
    }

    /**
    * ����down��Ϣ
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int sendKeyDown(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_DOWN;
        return sendMsg(cmd);
    }

    /**
    * ����ok��Ϣ
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int sendKeyOk(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_OK;
        return sendMsg(cmd);
    }

    /**
    * ����return��Ϣ
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int sendKeyReturn(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_RETURN;
        return sendMsg(cmd);
    }

    /**
    * �ػ�
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int powerOff(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_POWEROFF;
        return sendMsg(cmd);
    }

    /**
    * �����豸��ǰ�����ļ�����ʱ�䣬ͨ��EventListener��onRecvTotalTime�¼�����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int requestTotalTime(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_TOTAL_TIME;
        return sendMsg(cmd);
    }

    /**
    * �����豸��ǰ���Ž��ȣ�ͨ��EventListener��onRecvCurrentTime�¼�����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int requestCurrentTime(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_CURRENT_TIME;
        return sendMsg(cmd);
    }

    /**
    * �����豸��ǰ����״̬��ͨ��EventListener��onRecvPlayerStatus�¼�����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int requestPlayerStatus(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_PLAYER_STATUS;
        return sendMsg(cmd);
    }

    /**
    * ���ò���ѭ��ģʽ
    * @param seq ȡֵ LOOP_TYPE_SEQUENCE��LOOP_TYPE_LOOPALL��LOOP_TYPE_LOOPONE��LOOP_TYPE_RANDOM
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int setPlaySequence(int seq){
        String[] cmd = new String[4];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_SET_SEQUENCE;
        cmd[2] = MSG_KEY_SEQUENCE;
        cmd[3] = Integer.toString(seq);
        return sendMsg(cmd);
    }

    /**
    * �����豸��ǰ����˳��ͨ��EventListener��onRecvPlaySequence�¼�����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int requestPlaySequence(){
        String[] cmd = new String[2];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_SEQUENCE;
        return sendMsg(cmd);
    }

    /**
    * ����ͼƬ������ͼ������ͼ�յ���ͨ��EventListener����
    * @param urls ͼƬurl����
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int requestThumbnails(String[] urls){
        String[] cmd = new String[2];
        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_GET_THUMBNAIL;

        String data = "";
        for(int i=0; i<urls.length; i++){
            data += urls[i] + "\r\n";
        }
        return sendData(cmd, data.getBytes());
    }

    /**
    * ȡ��֮ǰ���������ͼ���Ա�ӿ��µ�����ͼ�Ļ�ȡ
     * @param urls ͼƬurl����
     * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int cancelThumbnails(String[] urls){
        String[] cmd = new String[2];
        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_CANCEL_THUMBNAIL;

        String data = "";
        for(int i=0; i<urls.length; i++){
            data += urls[i] + "\r\n";
        }
        return sendData(cmd, data.getBytes());
    }

    /**
    * �Ѿ��ϴ��ļ���֪ͨ���˸��²����б�
    * @param url �ļ�������е�·��
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public int onUploadFile(String url){
        String[] cmd = new String[4];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_ADD_FILE;
        cmd[2] = MSG_KEY_URL;
        cmd[3] = url;
        return sendMsg(cmd);
    }
    /**
    * �Ѿ�ɾ���ļ���֪ͨ���˸��²����б�
    * @param url �ļ�������е�·��
    * @return �ɹ�����0��ʧ�ܷ���-1
    * */
    public int onDeleteFile(String url){
        String[] cmd = new String[4];

        cmd[0] = MSG_KEY_CMD;
        cmd[1] = MSG_CMD_DELETE_FILE;
        cmd[2] = MSG_KEY_URL;
        cmd[3] = url;
        return sendMsg(cmd);
    }

    private static native void nativeSetup(Object cc);

    /**
    * ���ӷ���������ϢͨѶ�������ӳɹ�����ܷ�����Ϣ�����ӳɹ���ʧ��ͨ��EventListener����
    * @param ip Ҫ���ӵķ�������ip��ַ
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public native int connect(String ip);

    /**
    * �Ͽ�������������ӣ��Ͽ����޷�������Ϣ���������������˳�ʱ�ͷ���Դ
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public native int disconnect();

    /**
    * ���豸�˷�����Ϣ��
    * @param cmd ��Ϣ�ַ������飬��[key, value, key, value....]�ĸ�ʽ�ŷ�
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public native int sendMsg(Object[] cmd);

    /**
    * ���豸�˷�����Ϣ�Ͷ��������ݣ�
    * @param cmd ��Ϣ�ַ������飬��[key, value, key, value....]�ĸ�ʽ�ŷ�
    * @param data ��������������
    * @return �ɹ�����0��ʧ�ܷ���-1
    */
    public native int sendData(Object[] cmd, byte[] data);
}