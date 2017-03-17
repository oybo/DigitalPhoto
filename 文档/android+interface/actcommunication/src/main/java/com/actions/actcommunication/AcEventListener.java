package com.actions.actcommunication;

/**
 * ͨѶģ���¼������ӿ� <br>
 * Created by hexibin on 2016/5/23.
 */
public interface AcEventListener {
    /**
    * �����Ϸ�����
    */
    public void onDeviceConnected();
    /**
    * ��������Ͽ����ӣ��ײ����������������ӷ�����
    */
    public void onDeviceDisconnect();

    /**
    * ���յ��豸����ֵ��ȡֵ��Χ0~40
    * @param volume ����
    */
    public void onRecvVolume(int volume);
    /**
    * ���յ���ǰ��Ƶ��ʱ�䣬��λ����
    * @param timeMs ��ʱ��
    */
    public void onRecvTotalTime(int timeMs);
    /**
    * ���յ���ǰ����ʱ�䣬��λ����
    * @param timeMs ��ǰ����ʱ��.
    */
    public void onRecvCurrentTime(int timeMs);
    /**
    * ���յ���ǰ����״̬������״̬ȡֵ��PLAYER_STATUS_STOP��PLAYER_STATUS_PLAYING��PLAYER_STATUS_PAUSE��PLAYER_STATUS_ERROR
    * @param status ������״̬
    */
    public void onRecvPlayerStatus(int status);
    /**
    * ���յ���ǰ���ŵ�ѭ��ģʽ��ȡֵ LOOP_TYPE_SEQUENCE��LOOP_TYPE_LOOPALL��LOOP_TYPE_LOOPONE��LOOP_TYPE_RANDOM
    * @param seq ��ǰѭ��ģʽ
    */
    public void onRecvPlaySequence(int seq);
    /**
    * ���յ�ͼƬ����ͼ��
    * @param url  ͼƬ�ļ���url
    * @param data ����ͼ����������
    */
    public void onRecvThumbnail(String url, byte[] data);
}
