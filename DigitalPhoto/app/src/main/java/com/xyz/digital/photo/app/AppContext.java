package com.xyz.digital.photo.app;

import android.app.Application;

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
    }


}
