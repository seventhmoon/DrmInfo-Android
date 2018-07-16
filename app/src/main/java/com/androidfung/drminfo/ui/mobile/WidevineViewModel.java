package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.os.Build;

import com.androidfung.drminfo.MediaDrmFactory;
import com.androidfung.drminfo.R;

import androidx.annotation.NonNull;
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

            if (mMediaDrm != null) {
                prepareProperties(mMediaDrm);

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
    protected void prepareProperties(@NonNull MediaDrm mediaDrm) {
        ObservableArrayMap<String, Object> map = new ObservableArrayMap<>();
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
        mProperties.setValue(map);
    }

}
