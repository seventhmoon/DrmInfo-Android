package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.os.Build;

import com.androidfung.drminfo.MediaDrmFactory;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PrimetimeViewModel extends MediaDrmViewModel {

    private final static String TAG = PrimetimeViewModel.class.getSimpleName();

    public PrimetimeViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PRIMETIME);
    }

}
