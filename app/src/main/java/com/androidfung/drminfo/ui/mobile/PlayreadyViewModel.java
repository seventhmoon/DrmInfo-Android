package com.androidfung.drminfo.ui.mobile;

import android.app.Application;

import com.androidfung.drminfo.MediaDrmFactory;

public class PlayreadyViewModel extends MediaDrmViewModel {

    private final static String TAG = PlayreadyViewModel.class.getSimpleName();

    public PlayreadyViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PLAYREADY);
    }


}
