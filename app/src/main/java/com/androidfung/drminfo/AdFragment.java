package com.androidfung.drminfo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//
//import com.facebook.ads.Ad;
//import com.facebook.ads.AdError;
//import com.facebook.ads.AdSettings;
//import com.facebook.ads.AdView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class AdFragment extends Fragment {

    private static final String TAG = AdFragment.class.getSimpleName();

    private ViewGroup mAdHolder;
    private NativeExpressAdView mNativeExpressAdView;
    private com.facebook.ads.AdView mAdView;

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

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        remoteConfig.setDefaults(R.xml.remote_config_defaults);
        remoteConfig.setConfigSettings(configSettings);
        remoteConfig.fetch().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
//                Toast.makeText(getContext(), "Fetch Succeeded",
//                        Toast.LENGTH_SHORT).show();

                // After config data is successfully fetched, it must be activated before newly fetched
                // values are returned.
                remoteConfig.activateFetched();
//            } else {
//                Toast.makeText(this.getContext(), "Fetch Failed",
//                        Toast.LENGTH_SHORT).show();
            }
//            displayWelcomeMessage();
        });

        long adSource = remoteConfig.getLong("ad_source");
        Log.d(TAG, "ad_source: " + String.valueOf(adSource));
        Log.d(TAG, "current_approved_version: " + String.valueOf(remoteConfig.getLong("current_approved_version")));
        loadAd(adSource);


//        boolean displayAd = BuildConfig.VERSION_CODE <= remoteConfig.getLong("current_approved_version");

//        if (displayAd) {
//           loadAd();
//        }else{
//            mAdHolder.setVisibility(View.GONE);
//        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void loadAd(long adSource){
        if (adSource == 0L){
            mAdHolder.setVisibility(View.GONE);
        }else if (adSource == 1L) {
            loadAdMobNativeAd();
        }else if (adSource == 2L){
            loadAdMobBannerAd();
        }else {
            loadFbAd();
        }
    }

    private void loadFbAd(){
        com.facebook.ads.AdSettings.addTestDevice("879de71766c2d73162ec561c4e2ec35d");
        mAdView = new com.facebook.ads.AdView(this.getContext(), "738564012981533_738845662953368", com.facebook.ads.AdSize.RECTANGLE_HEIGHT_250);
        mAdHolder.addView(mAdView);
        mAdView.setAdListener(new com.facebook.ads.AdListener() {
            @Override
            public void onError(com.facebook.ads.Ad ad, com.facebook.ads.AdError adError) {
                Log.e(TAG, adError.getErrorMessage());
                mAdHolder.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded(com.facebook.ads.Ad ad) {
                mAdHolder.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClicked(com.facebook.ads.Ad ad) {

            }

            @Override
            public void onLoggingImpression(com.facebook.ads.Ad ad) {

            }
        });

        mAdView.loadAd();
//        mAdHolder.setVisibility(View.VISIBLE);
    }

    private void loadAdMobBannerAd(){
        com.google.android.gms.ads.AdView adView = new com.google.android.gms.ads.AdView(this.getActivity());
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Ads", "onAdLoaded");
                mAdHolder.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.i("Ads", "onAdFailedToLoad");
                mAdHolder.setVisibility(View.GONE);
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
        mAdHolder.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
//        mAdHolder.setVisibility(View.VISIBLE);
    }

    private void loadAdMobNativeAd() {
        if (mAdHolder != null) {
            mAdHolder.setVisibility(View.INVISIBLE);
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
        if (mAdView != null){
            mAdView.destroy();
        }
        super.onDestroy();
    }

}
