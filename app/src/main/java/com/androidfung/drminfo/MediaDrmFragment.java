package com.androidfung.drminfo;

import androidx.databinding.ObservableArrayMap;
import android.media.MediaDrm;
import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;

/**
 * Created by fung on 5/3/2016.
 */
@Keep
public class MediaDrmFragment extends Fragment {

    private static final String TAG = MediaDrmFragment.class.getSimpleName();

    protected ObservableArrayMap<String, Object> getMediaDrmProperties(MediaDrm mediaDrm){
        int[] keyIds = {R.string.key_media_drm_description, R.string.key_media_drm_version};

        ObservableArrayMap<String, Object> properties = new ObservableArrayMap<>();
        if (mediaDrm != null) {
            for (int id : keyIds) {
                try {
                    String key = getString(id);
                    String string = mediaDrm.getPropertyString(key);
                    properties.put(key, string);
                } catch (Exception e) {
                    //do nothing
                }

            }
        }
        return properties;
    }
}
