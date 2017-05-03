package com.xyz.digital.photo.app;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by O on 2017/3/17.
 */

public class AppContext extends Application {

    private static AppContext mInstance;

    public static AppContext getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance =this;

        CrashReport.initCrashReport(getApplicationContext(), "c5f1dd860f", false);

    }


}
