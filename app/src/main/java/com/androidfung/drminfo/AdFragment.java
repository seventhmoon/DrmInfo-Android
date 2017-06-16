package com.androidfung.drminfo;


import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class AdFragment extends Fragment {

    NativeExpressAdView mAdView;

    public AdFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_ad, container, false);

        mAdView = new NativeExpressAdView(getActivity());

        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // All emulators
                .addTestDevice("EBB53DEFEC528CC2A37BE4470FAB47B5")
                .addTestDevice("7336D37AD0AF5C7AC14EA15F457A0F58")
                .build();

        final ViewGroup adHolder = rootView.findViewById(R.id.ad_container_card);

        if (adHolder != null) {
            adHolder.post(() -> {
                float scaleRatio = getResources().getDisplayMetrics().density;
                int width = (int) (adHolder.getMeasuredWidth() / scaleRatio);
                int height = (int) (adHolder.getMeasuredHeight() / scaleRatio);
                mAdView.setAdSize(new AdSize(width, height));

                mAdView.setAdUnitId(getString(R.string.ad_unit_id_native));
                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        adHolder.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        adHolder.setVisibility(View.GONE);
                    }
                });
                mAdView.loadAd(adRequest);
                adHolder.addView(mAdView);
            });
        }

        return rootView;
    }


}
