package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;

import com.androidfung.drminfo.R;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public abstract class MediaDrmViewModel extends AndroidViewModel {
    private final static String TAG = MediaDrmViewModel.class.getSimpleName();
    protected MutableLiveData<ObservableArrayMap<String, Object>> mProperties;
    protected MediaDrm mMediaDrm;

    public MediaDrmViewModel(Application application) {
        super(application);
    }

    public LiveData<ObservableArrayMap<String, Object>> getProperties(){
        if (mProperties == null){
            mProperties = new MutableLiveData<>();
            prepareProperties(mMediaDrm);
        }
        return mProperties;
    }

    protected void prepareProperties(MediaDrm mediaDrm){
        if (mediaDrm != null) {
            ObservableArrayMap<String, Object> map = new ObservableArrayMap<>();
            String[] mediaDrmKeys = getApplication().getResources().getStringArray(R.array.mediadrm_string_properties_key);
            for (int i = 0; i < mediaDrmKeys.length; i++) {
                String value = mediaDrm.getPropertyString(mediaDrmKeys[i]);
                map.put(mediaDrmKeys[i], value);
            }
            mProperties.setValue(map);
        }
    }
}
