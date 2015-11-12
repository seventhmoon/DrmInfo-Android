package com.androidfung.drminfo;

import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.UUID;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public static final UUID WIDEVINE_UUID = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);
    public static final UUID PLAYREADY_UUID = new UUID(0x9A04F07998404286L, 0xAB92E65BE0885F95L);

    private MediaDrm mWidevineMediaDrm, mPlayreadyMediaDrm;
    private AdView mAdView;
    public MainActivityFragment() {

    }

    @SuppressWarnings("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        TextView textViewSupportPlayready = (TextView) rootView.findViewById(R.id.textview_support_playready);
        TextView textViewSupportWidevine = (TextView) rootView.findViewById(R.id.textview_support_widevine);
        TextView textViewSdkLevel = (TextView) rootView.findViewById(R.id.textview_sdk_level);
        TextView textViewModel = (TextView) rootView.findViewById(R.id.textview_model);
        TextView textViewBrand = (TextView) rootView.findViewById(R.id.textview_brand);
        TextView textViewProduct = (TextView) rootView.findViewById(R.id.textview_product);

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("DD39DD93271E8B083C34687B41D08D4C")
                .build();
        mAdView.loadAd(adRequest);

        textViewModel.setText(Build.MODEL);
        textViewSdkLevel.setText(String.valueOf(Build.VERSION.SDK_INT));
        textViewBrand.setText(Build.BRAND);
        textViewProduct.setText(Build.PRODUCT);

        try {
            mWidevineMediaDrm = new MediaDrm(WIDEVINE_UUID);
            mPlayreadyMediaDrm = new MediaDrm(PLAYREADY_UUID);
        } catch (UnsupportedSchemeException e) {
            e.printStackTrace();
        }

        if (mWidevineMediaDrm != null) {

            textViewSupportWidevine.setText(getString(R.string.text_supported_with_security_level, mWidevineMediaDrm.getPropertyString("securityLevel")));
        } else {
            textViewSupportWidevine.setText(R.string.text_not_supported);
        }

        if (mPlayreadyMediaDrm != null) {
            textViewSupportPlayready.setText(R.string.text_supported);
        } else {
            textViewSupportPlayready.setText(R.string.text_not_supported);
        }

        return rootView;
    }
}
