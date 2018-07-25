package com.androidfung.drminfo.ui.mobile;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.databinding.ObservableArrayMap;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidfung.drminfo.R;
import com.androidfung.drminfo.databinding.FragmentInfoBinding;

/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class InfoFragment extends Fragment {

    private final String TAG = InfoFragment.class.getSimpleName();
    private FragmentInfoBinding mBinding;

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedViewModel viewModel = ViewModelProviders.of(this.getActivity()).get(SharedViewModel.class);
        viewModel.getProperties().observe(this, mBinding::setMap);

        mBinding.setLifecycleOwner(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_info, container, false);

        return mBinding.getRoot();
    }
}
