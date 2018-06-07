package com.androidfung.drminfo;


import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayMap;
import androidx.databinding.ViewDataBinding;
import android.media.MediaDrm;
import android.os.Bundle;
import androidx.annotation.Keep;

import androidx.fragment.app.Fragment;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class WidevineFragment extends MediaDrmFragment {

    private static final String TAG = WidevineFragment.class.getSimpleName();

    private MediaDrm mWidevineMediaDrm;
    private ObservableArrayMap<String, Object> mProperties;

    public WidevineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
        mWidevineMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);
        mProperties = getMediaDrmProperties(mWidevineMediaDrm);

    }

    @SuppressWarnings("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_widevine, container, false);
        View rootView = binding.getRoot();
        binding.setVariable(com.androidfung.drminfo.BR.properties, mProperties);
        binding.setVariable(com.androidfung.drminfo.BR.mediaDrm, mWidevineMediaDrm);

        return rootView;
    }

    protected ObservableArrayMap<String, Object> getMediaDrmProperties(MediaDrm mediaDrm){
        int[] keyIds = {R.string.key_media_drm_description, R.string.key_media_drm_version,
        R.string.key_widevine_system_id, R.string.key_widevine_security_level, R.string.key_widevine_number_of_open_sessions,
        R.string.key_widevine_max_number_of_sessions, R.string.key_widevine_max_hdcp_level,
        R.string.key_widevine_hdcp_level, R.string.key_widevine_usage_reporting_support};

        ObservableArrayMap<String, Object> properties = new ObservableArrayMap<>();

        for (int id : keyIds){
            try {
                String key = getString(id);
                String string = mediaDrm.getPropertyString(key);
                properties.put(key, string);
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }

        }

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
//            int maxHdcpLevelLevel = mediaDrm.getMaxHdcpLevel();
//            properties.put(getString(R.string.key_widevine_max_hdcp_level), String.valueOf(maxHdcpLevelLevel));
//            int maxSessionCount = mediaDrm.getMaxSessionCount();
//            properties.put(getString(R.string.key_widevine_max_number_of_sessions), String.valueOf(maxSessionCount));
//
//            PersistableBundle metrics = mediaDrm.getMetrics();
//            if (metrics != null) {
//                Log.d(TAG, metrics.toString());
//            }
//        }


        return properties;
    }

}
