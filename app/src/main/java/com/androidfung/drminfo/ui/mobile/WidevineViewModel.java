package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.os.Build;

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
    protected void prepareProperties(MediaDrm mediaDrm) {
        super.prepareProperties(mediaDrm);
        ObservableArrayMap<String, Object> map = mProperties.getValue();
        if (mMediaDrm != null) {
            try {
                String[] wvKeys = getApplication().getResources().getStringArray(R.array.widevine_string_property_keys);

                for (String wvKey : wvKeys) {
                    String value = mediaDrm.getPropertyString(wvKey);
                    map.put(wvKey, value);
                }
            } catch (IllegalStateException | IllegalArgumentException ignored) {
            }
        }
        mProperties.setValue(map);
    }

}
