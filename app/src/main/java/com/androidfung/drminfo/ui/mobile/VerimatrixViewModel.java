package com.androidfung.drminfo.ui.mobile;

import android.app.Application;

import com.androidfung.drminfo.MediaDrmFactory;

public class VerimatrixViewModel extends MediaDrmViewModel {

    private final static String TAG = VerimatrixViewModel.class.getSimpleName();

    public VerimatrixViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.VERIMATRIX);
    }
    

}
