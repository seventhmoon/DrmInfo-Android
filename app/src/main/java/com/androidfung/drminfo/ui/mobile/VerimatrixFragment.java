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
import com.androidfung.drminfo.databinding.FragmentMediadrmBinding;
import com.androidfung.drminfo.databinding.FragmentVerimatrixBinding;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class VerimatrixFragment extends Fragment {

    private static final String TAG = VerimatrixFragment.class.getSimpleName();

    private FragmentMediadrmBinding mBinding;

    public VerimatrixFragment() {
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mediadrm, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MediaDrmViewModel viewModel = ViewModelProviders.of(this).get(VerimatrixViewModel.class);
        viewModel.getProperties().observe(this, mBinding::setProperties);

        mBinding.setLifecycleOwner(this);
    }

}
