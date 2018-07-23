package com.androidfung.drminfo.ui.mobile;

import android.app.Application;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SharedViewModel extends AndroidViewModel {

    private final static String TAG = SharedViewModel.class.getSimpleName();
    protected MutableLiveData<ObservableArrayMap<String, Object>> mProperties;

    public SharedViewModel(Application application) {
        super(application);
    }

    public LiveData<ObservableArrayMap<String, Object>> getProperties(){
        if (mProperties == null){
            mProperties = new MutableLiveData<>();

            ObservableArrayMap<String, Object> map = new ObservableArrayMap<>();

            mProperties.setValue(map);
        }

        return mProperties;
    }
}
