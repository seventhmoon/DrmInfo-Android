package com.androidfung.drminfo;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by fung.lam on 25/11/2015.
 */
public class MainApplication extends Application {

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
//        LeakCanary.install(this);
    }
}
