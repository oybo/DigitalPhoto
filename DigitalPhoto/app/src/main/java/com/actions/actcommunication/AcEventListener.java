package com.actions.actcommunication;

/**
 * 通讯模块事件监听接口 <br>
 * Created by hexibin on 2016/5/23.
 */
public interface AcEventListener {
    /**
     * 连接上服务器
     */
    public void onDeviceConnected();
    /**
     * 与服务器断开连接，底层会继续尝试重新连接服务器
     */
    public void onDeviceDisconnect();

    /**
     * 接收到设备音量值，取值范围0~40
     * @param volume 音量
     */
    public void onRecvVolume(int volume);
    /**
     * 接收到当前视频总时间，单位毫秒
     * @param timeMs 总时间
     */
    public void onRecvTotalTime(int timeMs);
    /**
     * 接收到当前播放时间，单位毫秒
     * @param timeMs 当前播放时间.
     */
    public void onRecvCurrentTime(int timeMs);
    /**
     * 接收到当前播放状态，播放状态取值：PLAYER_STATUS_STOP，PLAYER_STATUS_PLAYING，PLAYER_STATUS_PAUSE，PLAYER_STATUS_ERROR
     * @param status 播放器状态
     */
    public void onRecvPlayerStatus(int status);
    /**
     * 接收到当前播放的循环模式，取值 LOOP_TYPE_SEQUENCE、LOOP_TYPE_LOOPALL、LOOP_TYPE_LOOPONE、LOOP_TYPE_RANDOM
     * @param seq 当前循环模式
     */
    public void onRecvPlaySequence(int seq);
    /**
     * 接收到图片缩略图，
     * @param url  图片文件的url
     * @param data 缩略图二进制数据
     */
    public void onRecvThumbnail(String url, byte[] data);
}
