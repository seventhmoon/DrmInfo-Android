package com.androidfung.drminfo;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayMap;
import androidx.databinding.ViewDataBinding;
import android.media.MediaDrm;
import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class MarlinFragment extends MediaDrmFragment {

    private static final String TAG = MarlinFragment.class.getSimpleName();

    private MediaDrm mMarlinMediaDrm;
    private ObservableArrayMap<String, Object> mProperties;

    public MarlinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
        mMarlinMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.MARLIN);
        mProperties = getMediaDrmProperties(mMarlinMediaDrm);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewDataBinding  binding = DataBindingUtil.inflate(inflater, R.layout.fragment_marlin, container, false);
        View rootView = binding.getRoot();
        binding.setVariable(com.androidfung.drminfo.BR.mediaDrm, mMarlinMediaDrm);
        binding.setVariable(com.androidfung.drminfo.BR.properties, mProperties);
        return rootView;
    }


}
