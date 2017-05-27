package com.androidfung.drminfo;

import android.media.MediaDrm;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class ClearKeyFragment extends Fragment {

    private MediaDrm mMediaDrm;


    public ClearKeyFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);

        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.CLEARKEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_clearkey, container, false);
        final GridLayout gridLayoutPlayready = (GridLayout) rootView.findViewById(R.id.gridlayout_clearkey);
        final TextView textViewSupportPlayready = (TextView) rootView.findViewById(R.id.textview_support_clearkey);


        if (mMediaDrm != null) {
            textViewSupportPlayready.setText(R.string.text_supported);
            int[] headerIds = {R.string.text_drm_description, R.string.text_version};
            String[] keys = {MediaDrm.PROPERTY_DESCRIPTION, MediaDrm.PROPERTY_VERSION};

            for (int i = 0; i < headerIds.length; i++) {
                try {
                    AppCompatTextView textViewHeader = new AppCompatTextView(getContext());
                    AppCompatTextView textViewValue = new AppCompatTextView(getContext());
                    //                    textViewValue.setGravity(Gravity.RIGHT);

                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                    layoutParams.setGravity(Gravity.END);
                    textViewValue.setLayoutParams(layoutParams);

                    textViewHeader.setText(headerIds[i]);
                    textViewValue.setText(mMediaDrm.getPropertyString(keys[i]));

                    if (Build.VERSION.SDK_INT >= 23) {
                        textViewHeader.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    } else {
                        textViewHeader.setTextAppearance(getContext(), android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(getContext(), android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    }

                    gridLayoutPlayready.addView(textViewHeader);
                    gridLayoutPlayready.addView(textViewValue);
                    //                    gridLayoutWidevine.addView(textViewValue, );


                    //                    textViews[i].setText(mWidevineMediaDrm.getPropertyString(keys[i]));
                } catch (RuntimeException ex) {
//                    Log.d(TAG, ex.toString());
                }
            }

        } else {
            textViewSupportPlayready.setText(R.string.text_not_supported);
        }


        return rootView;
    }


}
