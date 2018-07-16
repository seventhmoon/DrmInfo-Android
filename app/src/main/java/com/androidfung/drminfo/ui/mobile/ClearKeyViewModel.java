package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.os.Build;

import com.androidfung.drminfo.MediaDrmFactory;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ClearKeyViewModel extends MediaDrmViewModel {

    private final static String TAG = ClearKeyViewModel.class.getSimpleName();

    public ClearKeyViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.CLEARKEY);
    }

}
