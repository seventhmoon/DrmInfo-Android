package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.os.Build;
import android.util.Log;

import com.androidfung.drminfo.MediaDrmFactory;
import com.androidfung.drminfo.R;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class WidevineViewModel extends MediaDrmViewModel {

    private final static String TAG = WidevineViewModel.class.getSimpleName();

    public WidevineViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);

    }

    @Override
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

    @Override
    protected void prepareProperties(MediaDrm mediaDrm) {
        ObservableArrayMap<String, Object> map = new ObservableArrayMap<>();
        if (mMediaDrm != null) {
            String[] mediaDrmKeys = getApplication().getResources().getStringArray(R.array.mediadrm_string_property_keys);
            for (String mediaDrmKey : mediaDrmKeys) {
                String value = mediaDrm.getPropertyString(mediaDrmKey);
                map.put(mediaDrmKey, value);
            }

            String[] wvKeys = getApplication().getResources().getStringArray(R.array.widevine_string_property_keys);
            for (String wvKey : wvKeys) {
                try {
                    String value = mediaDrm.getPropertyString(wvKey);
                    map.put(wvKey, value);
                } catch (IllegalStateException | IllegalArgumentException ignored) {
                }
            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                mMediaDrm.close();
//            } else {
//                mMediaDrm.release();
//            }

        }
        mProperties.setValue(map);
    }

}
