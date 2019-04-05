package com.androidfung.drminfo.ui.mobile;

import android.app.Application;

import com.androidfung.drminfo.Constants;

public class MarlinViewModel extends MediaDrmViewModel {

    private final static String TAG = MarlinViewModel.class.getSimpleName();

    public MarlinViewModel(Application application) {
        super(application);
        mUUID = Constants.MARLIN_UUID;
//        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.MARLIN);
    }

}
