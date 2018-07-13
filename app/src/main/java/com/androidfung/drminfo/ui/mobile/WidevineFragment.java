package com.androidfung.drminfo.ui.mobile;


import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import androidx.annotation.Keep;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidfung.drminfo.R;
import com.androidfung.drminfo.databinding.FragmentWidevineBinding;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class WidevineFragment extends Fragment {

    private static final String TAG = WidevineFragment.class.getSimpleName();

    private FragmentWidevineBinding mBinding;

    public WidevineFragment() {
        // Required empty public constructor
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_widevine, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setLifecycleOwner(this);

        WidevineViewModel mViewModel = ViewModelProviders.of(this).get(WidevineViewModel.class);
        mViewModel.getProperties().observe(this, mBinding::setProperties);
    }

}
