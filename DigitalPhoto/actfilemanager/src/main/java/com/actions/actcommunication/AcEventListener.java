package com.actions.actcommunication;

/**
 * Created by O on 2017/4/25.
 */

public interface AcEventListener {
    void onDeviceConnected();

    void onDeviceDisconnect();

    void onRecvVolume(int var1);

    void onRecvTotalTime(int var1);

    void onRecvCurrentTime(int var1);

    void onRecvPlayerStatus(int var1);

    void onRecvPlaySequence(int var1);

    void onRecvThumbnail(String var1, byte[] var2);

    void onRecvResult(String action, String[] status);

}
