package com.androidfung.drminfo.ui.mobile;

import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidfung.drminfo.BuildConfig;
import com.androidfung.drminfo.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class AdFragment extends Fragment {

    private static final String TAG = AdFragment.class.getSimpleName();

    private ViewGroup mBannerAdHolder;
    private AdView mAdView;
//    private NativeExpressAdView mNativeExpressAdView;

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

        mBannerAdHolder = rootView.findViewById(R.id.container_ad);
        mAdView = rootView.findViewById(R.id.adView);

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

        loadAd(adSource);
        return rootView;
    }

    private void loadAd(long adSource) {

        switch ((int) adSource) {
            case 9: {
                mBannerAdHolder.setVisibility(View.GONE);
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

    private void loadAdMobBannerAdPost() {
        mBannerAdHolder.post(() -> {
            //        if (mAdView != null) {
            AdView adView = new AdView(getContext());
            float scaleRatio = mBannerAdHolder.getResources().getDisplayMetrics().density;
            int width = (int) (mBannerAdHolder.getMeasuredWidth() / scaleRatio);
            int height = (int) (mBannerAdHolder.getMeasuredHeight() / scaleRatio);
            Log.d(TAG, "loadAdMobBannerAd: width=" + width + " height=" + height);
            adView.setAdSize(new AdSize(width, height));
            adView.setAdUnitId(getString(R.string.ad_unit_id_medium_rect));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.i("Ads", "onAdLoaded");
                    mBannerAdHolder.setVisibility(View.VISIBLE);
                    adView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    Log.i("Ads", "onAdFailedToLoad");
                    mBannerAdHolder.setVisibility(View.GONE);
                    adView.setVisibility(View.GONE);
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
            adView.loadAd(new AdRequest.Builder().build());
            mBannerAdHolder.addView(adView);
//        }
        });

    }

}
