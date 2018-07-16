package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;

import com.androidfung.drminfo.MediaDrmFactory;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final static String TAG = SharedViewModel.class.getSimpleName();
    protected MutableLiveData<ObservableArrayMap<String, Object>> mProperties;

//    public SharedViewModel(Application application) {
//        super(application);
//    }

    public LiveData<ObservableArrayMap<String, Object>> getProperties(){
        if (mProperties == null){
            mProperties = new MutableLiveData<>();

            ObservableArrayMap<String, Object> map = new ObservableArrayMap<>();

            MediaDrm widevineDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);
            MediaDrm playreadyDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PLAYREADY);
            MediaDrm verimatrixDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.VERIMATRIX);
            MediaDrm clearkeyDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.CLEARKEY);
            MediaDrm primetimeDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PRIMETIME);

            map.put("widevineDrm", widevineDrm);
            map.put("playreadyDrm", playreadyDrm);
            map.put("verimatrixDrm", verimatrixDrm);
            map.put("clearkeyDrm", clearkeyDrm);
            map.put("primetimeDrm", primetimeDrm);

            mProperties.setValue(map);
        }

        return mProperties;
    }
}
