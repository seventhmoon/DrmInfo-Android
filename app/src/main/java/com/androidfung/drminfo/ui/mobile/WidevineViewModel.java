package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;

import com.androidfung.drminfo.Constants;
import com.androidfung.drminfo.MediaDrmFactory;
import com.androidfung.drminfo.R;

import java.util.UUID;

import androidx.databinding.ObservableArrayMap;

public class WidevineViewModel extends MediaDrmViewModel {

    private final static String TAG = WidevineViewModel.class.getSimpleName();

    public WidevineViewModel(Application application) {
        super(application);
//        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);
        mUUID = Constants.WIDEVINE_UUID;
    }

    @Override
    protected void prepareProperties(MediaDrm mediaDrm) {
        super.prepareProperties(mediaDrm);
        ObservableArrayMap<String, Object> map = mProperties.getValue();
//        MediaDrm mediaDrm = super.getMediaDrm();
        if (mediaDrm != null) {
            try {
                String[] wvKeys = getApplication().getResources().getStringArray(R.array.widevine_string_property_keys);

                if (map != null) {
                    for (String wvKey : wvKeys) {
                        String value = mediaDrm.getPropertyString(wvKey);
                        if (map.get(wvKey) == null) {
                            map.put(wvKey, value);
                        }
                    }
                }
            } catch (IllegalStateException | IllegalArgumentException ignored) {
            }
        }
        mProperties.setValue(map);

    }

}
