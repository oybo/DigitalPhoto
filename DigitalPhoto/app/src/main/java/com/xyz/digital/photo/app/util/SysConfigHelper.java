package com.xyz.digital.photo.app.util;

import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.R;

/**
 * Created by O on 2017/5/18.
 */

public class SysConfigHelper {

    public static final String mImageShowScale_key = "photo_display_ratio";
    public static final String mPlayTime_key = "photo_slide_interval";
    public static final String mPlayOrder_key = "photo_play_mode";
    public static final String mVideoShowScale_key = "video_displaymode";
    public static final String mVideoPlayModel_key = "video_playmode";
    public static final String mAudioPlayModel_key = "music_play_mode";
    public static final String mStartPlayModel_key = "sys_startup_play";
    public static final String calendar_date = "calendar_date";
    public static final String mSelectLanguage_key = "select_language_key";
    public static final String calendar_alarm_freq = "calendar_alarm_freq";
    public static final String sys_auto_power_freq = "sys_auto_power_freq";

    public static final String mBg_Music_key = "photo_bgm";
    public static final String mBreakpointPlay_key = "video_resume_enable";
    public static final String mSubtitle_key = "video_subtitle";
    public static final String mShowSpectrum_key = "music_show_spectrum";
    public static final String mNz_states_key = "calendar_alarm_status";
    public static final String mNz_dskj_key = "sys_auto_poweron";
    public static final String mNz_dskj_off_key = "sys_auto_poweroff";

    public static String[] mImageShowScale = new String[]{AppContext.getInstance().getSString(R.string.quanping_txt),
            AppContext.getInstance().getSString(R.string.yscc_txt), AppContext.getInstance().getSString(R.string.dblsf_txt)};
    public static String[] mPlayTime = new String[]{AppContext.getInstance().getSString(R.string.time_sm_txt),
            AppContext.getInstance().getSString(R.string.time_wm_txt), AppContext.getInstance().getSString(R.string.time_swm_txt),
            AppContext.getInstance().getSString(R.string.time_ssm_txt), AppContext.getInstance().getSString(R.string.time_yfz_txt),
            AppContext.getInstance().getSString(R.string.time_wfz_txt), AppContext.getInstance().getSString(R.string.time_swfz_txt),
            AppContext.getInstance().getSString(R.string.time_ssfz_txt), AppContext.getInstance().getSString(R.string.time_yxs_txt)};
    public static String[] mPlayOrder = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
            .getInstance().getSString(R.string.suiji_txt)};
    public static String[] mVideoShowScale = new String[]{AppContext.getInstance().getSString(R.string.quanping_txt), AppContext
            .getInstance().getSString(R.string.yscc_txt), AppContext.getInstance().getSString(R.string.dblsf_txt)};
    public static String[] mVideoPlayModel = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
            .getInstance().getSString(R.string.sangecf_txt),
            AppContext.getInstance().getSString(R.string.quanbucf_txt), AppContext.getInstance().getSString(R.string.suiji_txt),
            AppContext.getInstance().getSString(R.string.suijijchongf_txt)};
    public static String[] mAudioPlayModel = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
            .getInstance().getSString(R.string.sangecf_txt),
            AppContext.getInstance().getSString(R.string.quanbucf_txt), AppContext.getInstance().getSString(R.string.suiji_txt),
            AppContext.getInstance().getSString(R.string.suijijchongf_txt)};
    public static String[] mStartPlayModel = new String[]{AppContext.getInstance().getSString(R.string.close_txt), AppContext
            .getInstance().getSString(R.string.image_txt),
            AppContext.getInstance().getSString(R.string.music_txt), AppContext.getInstance().getSString(R.string.video_txt), AppContext
            .getInstance().getSString(R.string.image_music_txt),
            AppContext.getInstance().getSString(R.string.set_calendar_txt)};
    public static String[] mAlarmFrequency = new String[]{ AppContext.getInstance().getSString(R.string.set_calendar_one),
            AppContext.getInstance().getSString(R.string.set_calendar_every_one),
            AppContext.getInstance().getSString(R.string.set_calendar_workday),
            AppContext.getInstance().getSString(R.string.set_calendar_weekend)};
    public static String[] mAutoPowerRequency = new String[]{ "Once", "Every Day", "Workday", "Weekend" };
    public static String[] mSelectLanguage = new String[] { "简体中文", "English"};


    public static void initTxt() {
        mImageShowScale = new String[]{AppContext.getInstance().getSString(R.string.quanping_txt),
                AppContext.getInstance().getSString(R.string.yscc_txt), AppContext.getInstance().getSString(R.string.dblsf_txt)};
        mPlayTime = new String[]{AppContext.getInstance().getSString(R.string.time_sm_txt),
                AppContext.getInstance().getSString(R.string.time_wm_txt), AppContext.getInstance().getSString(R.string.time_swm_txt),
                AppContext.getInstance().getSString(R.string.time_ssm_txt), AppContext.getInstance().getSString(R.string.time_yfz_txt),
                AppContext.getInstance().getSString(R.string.time_wfz_txt), AppContext.getInstance().getSString(R.string.time_swfz_txt),
                AppContext.getInstance().getSString(R.string.time_ssfz_txt), AppContext.getInstance().getSString(R.string.time_yxs_txt)};
        mPlayOrder = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt),
                AppContext.getInstance().getSString(R.string.sangecf_txt),
                AppContext.getInstance().getSString(R.string.quanbucf_txt),
                AppContext.getInstance().getSString(R.string.suiji_txt),
                AppContext.getInstance().getSString(R.string.suijijchongf_txt)};
        mVideoShowScale = new String[]{AppContext.getInstance().getSString(R.string.quanping_txt), AppContext
                .getInstance().getSString(R.string.yscc_txt), AppContext.getInstance().getSString(R.string.dblsf_txt)};
        mVideoPlayModel = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
                .getInstance().getSString(R.string.sangecf_txt),
                AppContext.getInstance().getSString(R.string.quanbucf_txt), AppContext.getInstance().getSString(R.string.suiji_txt),
                AppContext.getInstance().getSString(R.string.suijijchongf_txt)};
        mAudioPlayModel = new String[]{AppContext.getInstance().getSString(R.string.shunxu_txt), AppContext
                .getInstance().getSString(R.string.sangecf_txt),
                AppContext.getInstance().getSString(R.string.quanbucf_txt), AppContext.getInstance().getSString(R.string.suiji_txt),
                AppContext.getInstance().getSString(R.string.suijijchongf_txt)};
        mStartPlayModel = new String[]{AppContext.getInstance().getSString(R.string.close_txt), AppContext
                .getInstance().getSString(R.string.image_txt),
                AppContext.getInstance().getSString(R.string.music_txt), AppContext.getInstance().getSString(R.string.video_txt), AppContext
                .getInstance().getSString(R.string.image_music_txt),
                AppContext.getInstance().getSString(R.string.set_calendar_txt)};
        mSelectLanguage = new String[] { "简体中文", "English"};
    }
}
