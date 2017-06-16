package com.androidfung.drminfo;


import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.media.MediaDrm;
import android.os.Bundle;
import android.support.annotation.Keep;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class WidevineFragment extends Fragment {

    private MediaDrm mWidevineMediaDrm;

    public WidevineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
        mWidevineMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);
    }

    @SuppressWarnings("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_widevine, container, false);
        View rootView = binding.getRoot();

        binding.setVariable(BR.mediaDrm, mWidevineMediaDrm);

        return rootView;
    }

}
