package com.androidfung.drminfo;

//import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidfung.drminfo.databinding.ActivityMobileBinding;
import com.androidfung.drminfo.ui.mobile.ClearKeyViewModel;
import com.androidfung.drminfo.ui.mobile.MediaDrmViewModel;
import com.androidfung.drminfo.ui.mobile.PlayreadyViewModel;
import com.androidfung.drminfo.ui.mobile.WidevineViewModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

@Keep
public class MobileActivity extends AppCompatActivity {


    private static final String TAG = MobileActivity.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;
    private ActivityMobileBinding mBinding;
    private ViewGroup mBannerAdHolder;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_mobile);
        mBinding.setLifecycleOwner(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        MobileAds.initialize(this, getString(R.string.ad_app_id));

        MediaDrmViewModel clearKeyViewModel = ViewModelProviders.of(this).get(ClearKeyViewModel.class);
        clearKeyViewModel.getProperties().observe(this, mBinding::setClearkeyProperties);

        WidevineViewModel widevineViewModel = ViewModelProviders.of(this).get(WidevineViewModel.class);
        widevineViewModel.getProperties().observe(this, mBinding::setWidevineProperties);

        MediaDrmViewModel playreadyViewModel = ViewModelProviders.of(this).get(PlayreadyViewModel.class);
        playreadyViewModel.getProperties().observe(this, mBinding::setPlayreadyProperties);

        mBannerAdHolder = findViewById(R.id.container_ad);
        mAdView = findViewById(R.id.adView);

        loadAdMobBannerAd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
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

//    public void displayLicenseInformation(MenuItem item) {
//        try {
//            startActivity(new Intent(this, OssLicensesMenuActivity.class));
//        }catch (NullPointerException e){
//            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
//        }
//    }
}
