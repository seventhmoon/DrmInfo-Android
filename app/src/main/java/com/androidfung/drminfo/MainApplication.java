package com.androidfung.drminfo;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;

/**
 * Created by fung.lam on 25/11/2015.
 */
public class MainApplication extends MultiDexApplication  {

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
//        LeakCanary.install(this);
    }
}
