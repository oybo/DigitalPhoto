package com.xyz.digital.photo.app;

import android.app.Application;
import com.lzy.okgo.OkGo;

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

        //必须调用初始化
        OkGo.init(this);
    }


}
