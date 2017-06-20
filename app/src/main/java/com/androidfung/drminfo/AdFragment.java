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
import com.google.android.gms.ads.NativeExpressAdView;

import com.facebook.ads.*;

/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class AdFragment extends Fragment {

    private ViewGroup mAdHolder;
    private NativeExpressAdView mNativeExpressAdView;

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

        mAdHolder = rootView.findViewById(R.id.ad_container_card);
        mNativeExpressAdView = new NativeExpressAdView(getContext());

        loadAd();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void loadAd() {
        if (mAdHolder != null) {
            mAdHolder.post(() -> {
                try {
                    AdRequest adRequest = new AdRequest.Builder()
                            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // All emulators
                            .build();
                    float scaleRatio = mAdHolder.getResources().getDisplayMetrics().density;
                    int width = (int) (mAdHolder.getMeasuredWidth() / scaleRatio);
                    int height = (int) (mAdHolder.getMeasuredHeight() / scaleRatio);

                    mNativeExpressAdView.setAdSize(new AdSize(width, height));
                    mNativeExpressAdView.setAdUnitId(mAdHolder.getResources().getString(R.string.ad_unit_id_native));
                    mNativeExpressAdView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            mAdHolder.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            mAdHolder.setVisibility(View.GONE);
                        }
                    });

                    mNativeExpressAdView.loadAd(adRequest);
                    mAdHolder.addView(mNativeExpressAdView);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resume the NativeExpressAdView.
        if (mNativeExpressAdView != null) {
            mNativeExpressAdView.resume();
        }
    }

    @Override
    public void onPause() {
        // Pause the NativeExpressAdView.
        if (mNativeExpressAdView != null) {
            mNativeExpressAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // Destroy the NativeExpressAdView.
        if (mNativeExpressAdView != null) {
            mNativeExpressAdView.destroy();
        }
        super.onDestroy();
    }

}
