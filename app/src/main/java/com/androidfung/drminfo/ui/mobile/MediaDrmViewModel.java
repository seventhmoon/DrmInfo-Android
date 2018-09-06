package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.os.Build;
import android.util.Base64;

import com.androidfung.drminfo.R;

import androidx.annotation.RequiresApi;
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
//            if (mMediaDrm != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    mMediaDrm.close();
//                } else {
//                    mMediaDrm.release();
//                }
//            }
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
//                    int openSessionCount = mediaDrm.getOpenSessionCount();

                    map.put(getApplication().getString(R.string.mediadrm_key_maxNumberOfSessions), String.valueOf(maxSessionCount));
                    map.put(getApplication().getString(R.string.mediadrm_key_maxHdcpLevel), toStringValue(maxHdcpLevel));


                }
            } catch (IllegalArgumentException | IllegalStateException ignored) {
            }
        }
//        Log.d(TAG, map.toString());
        mProperties.setValue(map);

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private String toStringValue(@MediaDrm.HdcpLevel int maxHdcpLevel) {
        switch (maxHdcpLevel) {
            case MediaDrm.HDCP_LEVEL_UNKNOWN:
                return "Unknown";

            case MediaDrm.HDCP_NONE:
                return "None";

            case MediaDrm.HDCP_NO_DIGITAL_OUTPUT:
                return "No Digital Output";

            case MediaDrm.HDCP_V1:
                return "1";

            case MediaDrm.HDCP_V2_1:
                return "2.1";

            case MediaDrm.HDCP_V2_2:
                return "2.2";

            case MediaDrm.HDCP_V2:
                return "2";

            default:
                return null;
        }
    }
}
