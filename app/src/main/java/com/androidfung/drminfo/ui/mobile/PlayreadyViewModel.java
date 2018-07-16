package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.os.Build;

import com.androidfung.drminfo.MediaDrmFactory;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PlayreadyViewModel extends MediaDrmViewModel {

    private final static String TAG = PlayreadyViewModel.class.getSimpleName();

    public PlayreadyViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PLAYREADY);
    }

}
