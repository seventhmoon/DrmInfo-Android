package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class InfoViewModel extends AndroidViewModel {

    protected MutableLiveData<ObservableArrayMap<String, Object>> mProperties;

    protected InfoViewModel(Application application) {
        super(application);
    }

    public LiveData<ObservableArrayMap<String, Object>> getProperties() {
        if (mProperties == null) {
            mProperties = new MutableLiveData<>();
            ObservableArrayMap<String, Object> map = new ObservableArrayMap<>();

//            mProperties = Transformations.map(map, null);

            mProperties.setValue(map);
        }

        return mProperties;
    }
}
