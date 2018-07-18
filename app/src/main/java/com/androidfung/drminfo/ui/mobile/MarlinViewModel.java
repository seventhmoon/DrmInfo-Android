package com.androidfung.drminfo.ui.mobile;

import android.app.Application;

import com.androidfung.drminfo.MediaDrmFactory;

public class MarlinViewModel extends MediaDrmViewModel {

    private final static String TAG = MarlinViewModel.class.getSimpleName();

    public MarlinViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.MARLIN);
    }

}
