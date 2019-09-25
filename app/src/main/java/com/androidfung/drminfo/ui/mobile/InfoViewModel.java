package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.os.Build;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

//            map.put("sdkLevel", Build.VERSION.SDK_INT);
//            if (Build.VERSION.SDK_INT >= 21) {
//                map.put("abis", Build.SUPPORTED_ABIS);
//            }

            mProperties.setValue(map);
        }

        return mProperties;
    }
}
