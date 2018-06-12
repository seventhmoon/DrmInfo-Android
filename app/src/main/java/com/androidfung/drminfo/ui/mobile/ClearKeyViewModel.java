package com.androidfung.drminfo.ui.mobile;

import android.app.Application;

import com.androidfung.drminfo.MediaDrmFactory;

public class ClearKeyViewModel extends MediaDrmViewModel {

    private final static String TAG = ClearKeyViewModel.class.getSimpleName();

    public ClearKeyViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.CLEARKEY);
    }


}
