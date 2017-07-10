package com.androidfung.drminfo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdView;
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
    private AdView mAdView;

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
            loadAdMobAd();
        }else if (adSource == 2L){
            loadFbAd();
        }
    }

    private void loadFbAd(){

        mAdView = new AdView(this.getContext(), "738564012981533_738845662953368", com.facebook.ads.AdSize.RECTANGLE_HEIGHT_250);
        mAdHolder.addView(mAdView);
        mAdView.setAdListener(new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e(TAG, adError.getErrorMessage());
                mAdHolder.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                mAdHolder.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });
        mAdView.loadAd();
//        mAdHolder.setVisibility(View.VISIBLE);
    }

    private void loadAdMobAd() {
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
        if (mAdView != null){
            mAdView.destroy();
        }
        super.onDestroy();
    }

}
