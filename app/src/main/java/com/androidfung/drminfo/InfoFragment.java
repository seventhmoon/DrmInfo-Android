package com.androidfung.drminfo;


import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {


    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);


        TextView textViewSdkLevel = (TextView) rootView.findViewById(R.id.textview_sdk_level);
        TextView textViewModel = (TextView) rootView.findViewById(R.id.textview_model);
        TextView textViewBrand = (TextView) rootView.findViewById(R.id.textview_brand);
        TextView textViewProduct = (TextView) rootView.findViewById(R.id.textview_product);
        textViewModel.setText(Build.MODEL);
        textViewSdkLevel.setText(String.valueOf(Build.VERSION.SDK_INT));
        textViewBrand.setText(Build.BRAND);
        textViewProduct.setText(Build.PRODUCT);


        return rootView;

    }

}
