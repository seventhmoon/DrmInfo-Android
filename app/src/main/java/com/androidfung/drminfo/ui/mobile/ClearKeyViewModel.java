package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.util.Log;

import com.androidfung.drminfo.MediaDrmFactory;
import com.androidfung.drminfo.R;

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
