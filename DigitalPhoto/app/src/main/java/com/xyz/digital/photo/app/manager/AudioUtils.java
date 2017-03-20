package com.xyz.digital.photo.app.manager;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import com.xyz.digital.photo.app.bean.SongBean;
import java.util.ArrayList;

/**
 * Created by O on 2017/3/20.
 */

public class AudioUtils {

    /**
     * 获取sd卡所有的音乐文件
     *
     * @return
     * @throws Exception
     */
    public static ArrayList<SongBean> getAllSongs(Context context) {

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DATA
                },
                MediaStore.Audio.Media.MIME_TYPE + "=? or "
                        + MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[]{"audio/mpeg", "audio/x-ms-wma"}, null);

        ArrayList<SongBean> songs = new ArrayList<SongBean>();

        try {
            SongBean song = null;
            while (cursor.moveToNext()) {
                song = new SongBean();
                // 文件名
                song.setFileName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                // 歌曲名
                song.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                // 时长
                song.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                // 歌手名
                song.setSinger(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                // 专辑名
                song.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                // 文件路径
                song.setFileUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));

                songs.add(song);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }

        return songs;
    }

}
