package com.androidfung.drminfo;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayMap;
import android.databinding.ViewDataBinding;
import android.media.MediaDrm;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
@Keep
public class PlayreadyFragment extends MediaDrmFragment {

    private static final String TAG = PlayreadyFragment.class.getSimpleName();

    private MediaDrm mPlayreadyMediaDrm;
    private ObservableArrayMap<String, Object> mProperties;

    public PlayreadyFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);

        mPlayreadyMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PLAYREADY);
        mProperties = getMediaDrmProperties(mPlayreadyMediaDrm);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewDataBinding  binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playready, container, false);
        View rootView = binding.getRoot();
        binding.setVariable(com.androidfung.drminfo.BR.mediaDrm, mPlayreadyMediaDrm);
        binding.setVariable(com.androidfung.drminfo.BR.properties, mProperties);

        return rootView;
    }


}
