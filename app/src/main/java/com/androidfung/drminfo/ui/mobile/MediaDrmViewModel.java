package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
import android.os.Build;
import android.util.Base64;

import com.androidfung.drminfo.R;

import java.util.UUID;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.androidfung.drminfo.Util.toHdcpLevelString;

public abstract class MediaDrmViewModel extends AndroidViewModel {

    private final static String TAG = MediaDrmViewModel.class.getSimpleName();
    protected MutableLiveData<ObservableArrayMap<String, Object>> mProperties;

    private MediaDrm mMediaDrm;
    protected UUID mUUID;

    protected MediaDrmViewModel(Application application) {
        super(application);
    }

    public LiveData<ObservableArrayMap<String, Object>> getProperties() {
        if (mProperties == null) {
            mProperties = new MutableLiveData<>();

            if (MediaDrm.isCryptoSchemeSupported(mUUID)) {
                try {
                    mMediaDrm = new MediaDrm(mUUID);
                    prepareProperties(mMediaDrm);
                } catch (UnsupportedSchemeException e) {
                    e.printStackTrace();
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
                    String encodedString = Base64.encodeToString(value, Base64.DEFAULT).toUpperCase().trim();
                    map.put(mediaDrmKey, encodedString);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    int maxHdcpLevel = mediaDrm.getMaxHdcpLevel();
                    int maxSessionCount = mediaDrm.getMaxSessionCount();
                    int connectedHdcpLevel = mediaDrm.getConnectedHdcpLevel();
//                    int openSessionCount = mediaDrm.getOpenSessionCount();

                    map.put(getApplication().getString(R.string.mediadrm_key_maxNumberOfSessions), String.valueOf(maxSessionCount));
                    map.put(getApplication().getString(R.string.mediadrm_key_maxHdcpLevel), toHdcpLevelString(maxHdcpLevel));
                    map.put(getApplication().getString(R.string.mediadrm_key_hdcpLevel), toHdcpLevelString(connectedHdcpLevel));

                }
            } catch (IllegalArgumentException | IllegalStateException ignored) {
            }
        }
//        Log.d(TAG, map.toString());
        mProperties.setValue(map);

    }



    protected void onCleared() {
        if (mMediaDrm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                this.mMediaDrm.close();
            } else {
                this.mMediaDrm.release();
            }
        }
    }

}
