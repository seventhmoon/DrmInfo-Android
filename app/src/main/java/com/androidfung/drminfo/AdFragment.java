package com.androidfung.drminfo;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class AdFragment extends Fragment {

    public AdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_ad, container, false);

        ViewGroup adHolder = rootView.findViewById(R.id.ad_container_card);
        loadAd(adHolder);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void loadAd(ViewGroup adHolder) {
        if (adHolder != null) {
            adHolder.post(() -> {
                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // All emulators
                        .build();
                float scaleRatio = adHolder.getResources().getDisplayMetrics().density;
                int width = (int) (adHolder.getMeasuredWidth() / scaleRatio);
                int height = (int) (adHolder.getMeasuredHeight() / scaleRatio);

                NativeExpressAdView adView = new NativeExpressAdView(getContext());
                adView.setAdSize(new AdSize(width, height));
                adView.setAdUnitId(adHolder.getResources().getString(R.string.ad_unit_id_native));
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        adHolder.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        adHolder.setVisibility(View.GONE);
                    }
                });

                adView.loadAd(adRequest);
                adHolder.addView(adView);
            });
        }
    }


}
