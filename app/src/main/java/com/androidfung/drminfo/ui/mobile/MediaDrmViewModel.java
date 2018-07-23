package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.os.Build;

import com.androidfung.drminfo.R;
import com.google.common.io.BaseEncoding;

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

    public LiveData<ObservableArrayMap<String, Object>> getProperties() {
        if (mProperties == null) {
            mProperties = new MutableLiveData<>();
            prepareProperties(mMediaDrm);
            if (mMediaDrm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    mMediaDrm.close();
                } else {
                    mMediaDrm.release();
                }
            }
        }

        return mProperties;
    }

    protected void prepareProperties(MediaDrm mediaDrm) {
        ObservableArrayMap<String, Object> map = new ObservableArrayMap<>();
        if (mediaDrm != null) {
            try {
                String[] mediaDrmKeys = getApplication().getResources().getStringArray(R.array.mediadrm_string_property_keys);
                for (String mediaDrmKey : mediaDrmKeys) {
                    String value = mediaDrm.getPropertyString(mediaDrmKey);
                    map.put(mediaDrmKey, value);
                }

                String[] mediaDrmKeys2 = getApplication().getResources().getStringArray(R.array.mediadrm_bytearray_property_keys);
                for (String mediaDrmKey : mediaDrmKeys2) {
                    byte[] value = mediaDrm.getPropertyByteArray(mediaDrmKey);
                    map.put(mediaDrmKey, BaseEncoding.base64().encode(value).toUpperCase());
                }
            } catch (IllegalArgumentException | IllegalStateException ignored) {
            }
        }
        mProperties.setValue(map);

    }
}
