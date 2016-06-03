package com.androidfung.drminfo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MobileActivity extends AppCompatActivity {

    private static final String TAG = MobileActivity.class.getSimpleName();
        private NativeExpressAdView mAdView;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(getApplicationContext(), getString(R.string.ad_unit_id_native));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, new Bundle());

        mAdView = new NativeExpressAdView(this);

        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // All emulators
                .build();

        final ViewGroup adHolder = (ViewGroup) findViewById(R.id.ad_container_card);
        if (adHolder != null) {
            adHolder.post(new Runnable() {
                @Override
                public void run() {
                    float scaleRatio = getResources().getDisplayMetrics().density;
                    int width = (int)(adHolder.getMeasuredWidth()/scaleRatio);
                    int height = (int) (adHolder.getMeasuredHeight()/scaleRatio);
                    mAdView.setAdSize(new AdSize(width, height));
                    mAdView.setAdUnitId(getString(R.string.ad_unit_id_native));
                    Log.d(TAG, "" + (int)(adHolder.getMeasuredWidth()/scaleRatio) + ", " + (int)(adHolder.getMeasuredHeight()/scaleRatio));
                    mAdView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            // Save app state before going to the ad overlay.
                            View container = findViewById(R.id.ad_container_card);
                            if (container != null) {
                                container.setVisibility(View.VISIBLE);
                            }

                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode){
                            View container = findViewById(R.id.ad_container_card);
                            if (container != null) {
                                container.setVisibility(View.GONE);
                            }
                        }
                    });
                    mAdView.loadAd(adRequest);
                    adHolder.addView(mAdView);
                }
            });
        }

//        if (adView != null) {
//            AdSize adSize = new AdSize((int) mAdView.getX(), AdSize.AUTO_HEIGHT);
//            mAdView.setAdSize(adSize);
//            mAdView.setAdUnitId(getString(R.string.ad_unit_id_native));
//            adView.setAdListener(new AdListener() {
//                @Override
//                public void onAdLoaded() {
//                    // Save app state before going to the ad overlay.
//                    View container = findViewById(R.id.ad_container_card);
//                    if (container != null){
//                        container.setVisibility(View.VISIBLE);
//                    }
//
//                }
//            });
//            mAdView.loadAd(adRequest);
//        }


//        // Get tracker.
//        Tracker t = ((MainApplication) getApplication()).getTracker();
//
//        // Set screen name.
//        t.setScreenName(TAG);
//
//        // Send a screen view.
//        t.send(new HitBuilders.ScreenViewBuilder().build());


//        getAdLoaderNativeAds().loadAd(adRequest);
    }



}
