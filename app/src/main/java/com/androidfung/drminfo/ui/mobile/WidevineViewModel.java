package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.os.PersistableBundle;
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
        }
        prepareProperties(mMediaDrm);
        return mProperties;
    }

    @Override
    protected void prepareProperties(MediaDrm mediaDrm) {
        super.prepareProperties(mediaDrm);
        ObservableArrayMap<String, Object> map = mProperties.getValue();

        String[] wvKeys = getApplication().getResources().getStringArray(R.array.widevine_string_properties);

        if (mediaDrm != null) {
            for (int i = 0; i < wvKeys.length; i++) {
                try {
                    String value = mediaDrm.getPropertyString(wvKeys[i]);
                    map.put(wvKeys[i], value);
                }catch (IllegalStateException e){
                }
            }
        }
    }

}
