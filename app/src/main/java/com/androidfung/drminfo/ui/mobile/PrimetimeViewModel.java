package com.androidfung.drminfo.ui.mobile;

import android.app.Application;

import com.androidfung.drminfo.MediaDrmFactory;

public class PrimetimeViewModel extends MediaDrmViewModel {

    private final static String TAG = PrimetimeViewModel.class.getSimpleName();

    public PrimetimeViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PRIMETIME);
    }



}
