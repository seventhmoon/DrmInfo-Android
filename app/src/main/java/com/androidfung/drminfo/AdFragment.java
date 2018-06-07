package com.androidfung.drminfo;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class AdFragment extends Fragment {

    private static final String TAG = AdFragment.class.getSimpleName();

    private ViewGroup mBannerAdHolder, mNativeAdHolder;
    private AdView mAdView;
    private NativeExpressAdView mNativeExpressAdView;

    public AdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileAds.initialize(getActivity(), getString(R.string.ad_app_id));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_ad, container, false);

        mNativeAdHolder = mBannerAdHolder = rootView.findViewById(R.id.container_ad);
        mAdView = rootView.findViewById(R.id.adView);
        mNativeExpressAdView = new NativeExpressAdView(getContext());

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        remoteConfig.setDefaults(R.xml.remote_config_defaults);
        remoteConfig.setConfigSettings(configSettings);
        remoteConfig.fetch().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                remoteConfig.activateFetched();
            }
        });

        long adSource = remoteConfig.getLong("ad_source");
        Log.d(TAG, "ad_source: " + String.valueOf(adSource));
        Log.d(TAG, "current_approved_version: " + String.valueOf(remoteConfig.getLong("current_approved_version")));


        loadAd(adSource);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void loadAd(long adSource) {

        switch ((int) adSource) {
            case 9: {
                mBannerAdHolder.setVisibility(View.GONE);
                mNativeAdHolder.setVisibility(View.GONE);
                break;
            }
            case 2: {
                loadAdMobBannerAd();
                break;
            }
            default:
                loadAdMobBannerAd();
        }

    }

    private void loadAdMobBannerAd() {

        if (mAdView != null) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.i("Ads", "onAdLoaded");
                    mBannerAdHolder.setVisibility(View.VISIBLE);
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    Log.i("Ads", "onAdFailedToLoad");
                    mBannerAdHolder.setVisibility(View.GONE);
                    mAdView.setVisibility(View.GONE);
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                    Log.i("Ads", "onAdOpened");
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                    Log.i("Ads", "onAdLeftApplication");
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                    Log.i("Ads", "onAdClosed");
                }
            });
            mAdView.loadAd(new AdRequest.Builder().build());
        }
    }

    @Deprecated
    private void loadAdMobNativeAd() {
        if (mNativeAdHolder != null) {
            mNativeAdHolder.setVisibility(View.INVISIBLE);
            mNativeAdHolder.post(() -> {
                try {
                    AdRequest adRequest = new AdRequest.Builder()
                            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // All emulators
                            .build();
                    float scaleRatio = mNativeAdHolder.getResources().getDisplayMetrics().density;
                    int width = (int) (mNativeAdHolder.getMeasuredWidth() / scaleRatio);
                    int height = (int) (mNativeAdHolder.getMeasuredHeight() / scaleRatio);
                    Log.d(TAG, "loadAdMobNativeAd: width=" + width + " height=" + height);
                    mNativeExpressAdView.setAdSize(new AdSize(width, height));
                    mNativeExpressAdView.setAdUnitId(mNativeAdHolder.getResources().getString(R.string.ad_unit_id_native));
                    mNativeExpressAdView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            mNativeAdHolder.setVisibility(View.VISIBLE);
                            mAdView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            Log.e(TAG, "onAdFailedToLoad: " + errorCode);
                            mNativeAdHolder.setVisibility(View.GONE);
                            loadAdMobBannerAd();
                        }
                    });

                    mNativeExpressAdView.loadAd(adRequest);
                    mNativeAdHolder.addView(mNativeExpressAdView);
                } catch (NullPointerException e) {
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
