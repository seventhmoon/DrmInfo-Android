package com.androidfung.drminfo;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class InfoFragment extends Fragment{

    private final String TAG = InfoFragment.class.getSimpleName();
    private View mRootView;

    public InfoFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_info, container, false);

        TextView textViewSdkLevel = (TextView) mRootView.findViewById(R.id.textview_sdk_level);
        TextView textViewModel = (TextView) mRootView.findViewById(R.id.textview_model);
        TextView textViewBrand = (TextView) mRootView.findViewById(R.id.textview_brand);
        TextView textViewProduct = (TextView) mRootView.findViewById(R.id.textview_product);
        textViewModel.setText(Build.MODEL);
        textViewSdkLevel.setText(String.valueOf(Build.VERSION.SDK_INT));
        textViewBrand.setText(Build.BRAND);
        textViewProduct.setText(Build.PRODUCT);

        return mRootView;

    }

}
