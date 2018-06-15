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
import com.androidfung.drminfo.databinding.FragmentClearkeyBinding;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
@Keep
public class ClearKeyFragment extends Fragment {

    private static final String TAG = ClearKeyFragment.class.getSimpleName();

    private MediaDrmViewModel mViewModel;
    private FragmentClearkeyBinding mBinding;


    public ClearKeyFragment() {
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_clearkey, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(ClearKeyViewModel.class);
        mViewModel.getProperties().observe(this, mBinding::setProperties);

        mBinding.setLifecycleOwner(this);
    }
}
