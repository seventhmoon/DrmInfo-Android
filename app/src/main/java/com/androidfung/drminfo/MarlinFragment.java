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
public class MarlinFragment extends Fragment {

    private MediaDrm mMarlinMediaDrm;

    public MarlinFragment() {
        // Required empty public constructor
        mMarlinMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.MARLIN);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewDataBinding  binding = DataBindingUtil.inflate(inflater, R.layout.fragment_marlin, container, false);
        View rootView = binding.getRoot();
        binding.setVariable(BR.mediaDrm, mMarlinMediaDrm);

        return rootView;
    }

}
