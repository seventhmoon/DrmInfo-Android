package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.os.Build;

import com.androidfung.drminfo.MediaDrmFactory;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MarlinViewModel extends MediaDrmViewModel {

    private final static String TAG = MarlinViewModel.class.getSimpleName();

    public MarlinViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.MARLIN);
    }

}
