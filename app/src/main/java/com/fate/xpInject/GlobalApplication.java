package com.fate.xpInject;

import android.app.Application;

/**
 * Created by jy on 2018/12/17.
 */

public class GlobalApplication extends Application {
    private static GlobalApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static GlobalApplication getInstance() {

        if (instance == null)
            instance = new GlobalApplication();

        return instance;
    }

}