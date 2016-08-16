package com.androidfung.drminfo;

import android.media.MediaDrm;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

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

    private MediaDrm mWidevineMediaDrm;

    private MediaDrm mPlayreadyMediaDrm;
    private MediaDrm mVerimatrixMediaDrm;
    private MediaDrm mMarlinMediaDrm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


//        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridlayout_holder);


        initFirebase();
//        prepareMediaDrm();
//        loadAd();
//        loadInfo();
//        loadWidevine();
//        loadPlayReady();
//        loadMarlin();
//        loadVerimatrix();
    }


    private void loadMarlin() {
        final GridLayout gridLayoutMarlin = (GridLayout) findViewById(R.id.gridlayout_marlin);
        final TextView textViewSupportMarlin = (TextView) findViewById(R.id.textview_support_marlin);

        if (mMarlinMediaDrm != null) {
            textViewSupportMarlin.setText(R.string.text_supported);
            int[] headerIds = {R.string.text_drm_description, R.string.text_version};
            String[] keys = {MediaDrm.PROPERTY_DESCRIPTION, MediaDrm.PROPERTY_VERSION};

            for (int i = 0; i < headerIds.length; i++) {
                try {
                    AppCompatTextView textViewHeader = new AppCompatTextView(this);
                    AppCompatTextView textViewValue = new AppCompatTextView(this);
                    //                    textViewValue.setGravity(Gravity.RIGHT);

                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                    layoutParams.setGravity(Gravity.END);
                    textViewValue.setLayoutParams(layoutParams);


                    textViewHeader.setText(headerIds[i]);
                    textViewValue.setText(mMarlinMediaDrm.getPropertyString(keys[i]));

                    if (Build.VERSION.SDK_INT >= 23) {
                        textViewHeader.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    } else {
                        textViewHeader.setTextAppearance(this, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(this, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    }

                    gridLayoutMarlin.addView(textViewHeader);
                    gridLayoutMarlin.addView(textViewValue);
                    //                    gridLayoutWidevine.addView(textViewValue, );


                    //                    textViews[i].setText(mWidevineMediaDrm.getPropertyString(keys[i]));
                } catch (IllegalStateException ex) {
//                    Log.d(TAG, ex.toString());
                }
            }

        } else {
            textViewSupportMarlin.setText(R.string.text_not_supported);
        }

    }

    private void loadVerimatrix() {
        final GridLayout gridLayoutVerimatrix = (GridLayout) findViewById(R.id.gridlayout_verimatrix);
        final TextView textViewSupportVerimatrix = (TextView) findViewById(R.id.textview_support_verimatirx);


        if (mVerimatrixMediaDrm != null) {
            textViewSupportVerimatrix.setText(R.string.text_supported);
            int[] headerIds = {R.string.text_drm_description, R.string.text_version};
            String[] keys = {MediaDrm.PROPERTY_DESCRIPTION, MediaDrm.PROPERTY_VERSION};

            for (int i = 0; i < headerIds.length; i++) {
                try {
                    AppCompatTextView textViewHeader = new AppCompatTextView(this);
                    AppCompatTextView textViewValue = new AppCompatTextView(this);
                    //                    textViewValue.setGravity(Gravity.RIGHT);

                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                    layoutParams.setGravity(Gravity.END);
                    textViewValue.setLayoutParams(layoutParams);


                    textViewHeader.setText(headerIds[i]);
                    textViewValue.setText(mVerimatrixMediaDrm.getPropertyString(keys[i]));

                    if (Build.VERSION.SDK_INT >= 23) {
                        textViewHeader.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    } else {
                        textViewHeader.setTextAppearance(this, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(this, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    }

                    gridLayoutVerimatrix.addView(textViewHeader);
                    gridLayoutVerimatrix.addView(textViewValue);
                    //                    gridLayoutWidevine.addView(textViewValue, );


                    //                    textViews[i].setText(mWidevineMediaDrm.getPropertyString(keys[i]));
                } catch (IllegalStateException ex) {
//                    Log.d(TAG, ex.toString());
                }
            }

        } else {
            textViewSupportVerimatrix.setText(R.string.text_not_supported);
        }

    }

    private void prepareMediaDrm() {
        mWidevineMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);
        mPlayreadyMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PLAYREADY);
        mVerimatrixMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.VERIMATRIX);
        mMarlinMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.MARLIN);

    }

    private void loadPlayReady() {
        final GridLayout gridLayoutPlayready = (GridLayout) findViewById(R.id.gridlayout_playready);
        final TextView textViewSupportPlayready = (TextView) findViewById(R.id.textview_support_playready);


        if (mPlayreadyMediaDrm != null) {
            textViewSupportPlayready.setText(R.string.text_supported);
            int[] headerIds = {R.string.text_drm_description, R.string.text_version};
            String[] keys = {MediaDrm.PROPERTY_DESCRIPTION, MediaDrm.PROPERTY_VERSION};

            for (int i = 0; i < headerIds.length; i++) {
                try {
                    AppCompatTextView textViewHeader = new AppCompatTextView(this);
                    AppCompatTextView textViewValue = new AppCompatTextView(this);
                    //                    textViewValue.setGravity(Gravity.RIGHT);

                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                    layoutParams.setGravity(Gravity.END);
                    textViewValue.setLayoutParams(layoutParams);

                    textViewHeader.setText(headerIds[i]);
                    textViewValue.setText(mPlayreadyMediaDrm.getPropertyString(keys[i]));

                    if (Build.VERSION.SDK_INT >= 23) {
                        textViewHeader.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    } else {
                        textViewHeader.setTextAppearance(this, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(this, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    }

                    gridLayoutPlayready.addView(textViewHeader);
                    gridLayoutPlayready.addView(textViewValue);
                    //                    gridLayoutWidevine.addView(textViewValue, );


                    //                    textViews[i].setText(mWidevineMediaDrm.getPropertyString(keys[i]));
                } catch (IllegalStateException ex) {
//                    Log.d(TAG, ex.toString());
                }
            }

        } else {
            textViewSupportPlayready.setText(R.string.text_not_supported);
        }

    }

    private void initFirebase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, new Bundle());
    }

    private void loadAd() {
        MobileAds.initialize(getApplicationContext(), getString(R.string.ad_unit_id_native));

        mAdView = new NativeExpressAdView(this);

        final AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // All emulators
                .addTestDevice("EBB53DEFEC528CC2A37BE4470FAB47B5")
                .addTestDevice("7336D37AD0AF5C7AC14EA15F457A0F58")
                .build();
        final ViewGroup adHolder = (ViewGroup) findViewById(R.id.ad_container_card);
        if (adHolder != null) {
            adHolder.post(new Runnable() {
                @Override
                public void run() {
                    float scaleRatio = getResources().getDisplayMetrics().density;
                    int width = (int) (adHolder.getMeasuredWidth() / scaleRatio);
                    int height = (int) (adHolder.getMeasuredHeight() / scaleRatio);
                    mAdView.setAdSize(new AdSize(width, height));
                    mAdView.setAdUnitId(getString(R.string.ad_unit_id_native));
                    Log.d(TAG, "" + (int) (adHolder.getMeasuredWidth() / scaleRatio) + ", " + (int) (adHolder.getMeasuredHeight() / scaleRatio));
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
                        public void onAdFailedToLoad(int errorCode) {
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
    }

    private void loadInfo() {


        TextView textViewSdkLevel = (TextView) findViewById(R.id.textview_sdk_level);
        TextView textViewModel = (TextView) findViewById(R.id.textview_model);
        TextView textViewBrand = (TextView) findViewById(R.id.textview_brand);
        TextView textViewProduct = (TextView) findViewById(R.id.textview_product);
        textViewModel.setText(Build.MODEL);
        textViewSdkLevel.setText(String.valueOf(Build.VERSION.SDK_INT));
        textViewBrand.setText(Build.BRAND);
        textViewProduct.setText(Build.PRODUCT);


    }

    @SuppressWarnings("ResourceType")
    private void loadWidevine() {
        final TextView textViewSupportWidevine = (TextView) findViewById(R.id.textview_support_widevine);
        final android.widget.GridLayout gridLayoutWidevine = (android.widget.GridLayout) findViewById(R.id.gridlayout_widevine);


        if (mWidevineMediaDrm != null) {

            textViewSupportWidevine.setText(R.string.text_supported);

//            TextView[] textViews = {textViewWidevineVersion, textViewWidevineSystemId, textViewWidevineSecurityLevel, textViewWidevineHdcpLevel, textViewWidevineMaxHdcpLevel, textViewWidevineUsageReportingSupport, textViewWidevineMaxNumberOfSessions, textViewWidevineNumberOfOpenSessions};
            int[] headerIds = {R.string.text_version, R.string.text_widevine_system_id, R.string.text_widevine_security_level, R.string.text_widevine_hdcp_level, R.string.text_widevine_max_hdcp_level, R.string.text_widevine_usage_reporting_support, R.string.text_widevine_maximum_number_of_sessions, R.string.text_widevine_number_of_open_sessions};
            String[] keys = {MediaDrm.PROPERTY_VERSION, "systemId", "securityLevel", "hdcpLevel", "maxHdcpLevel", "usageReportingSupport", "maxNumberOfSessions", "numberOfOpenSessions"};

            for (int i = 0; i < headerIds.length; i++) {
                try {
                    AppCompatTextView textViewHeader = new AppCompatTextView(this);
                    AppCompatTextView textViewValue = new AppCompatTextView(this);
//                    textViewValue.setGravity(Gravity.RIGHT);

                    android.widget.GridLayout.LayoutParams layoutParams = new android.widget.GridLayout.LayoutParams();
                    layoutParams.setGravity(Gravity.END);
                    textViewValue.setLayoutParams(layoutParams);


                    textViewHeader.setText(headerIds[i]);
                    textViewValue.setText(mWidevineMediaDrm.getPropertyString(keys[i]));

                    if (Build.VERSION.SDK_INT >= 23) {
                        textViewHeader.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    } else {
                        textViewHeader.setTextAppearance(this, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(this, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    }

                    gridLayoutWidevine.addView(textViewHeader);
                    gridLayoutWidevine.addView(textViewValue);
//                    gridLayoutWidevine.addView(textViewValue, );


//                    textViews[i].setText(mWidevineMediaDrm.getPropertyString(keys[i]));
                } catch (IllegalStateException ex) {
//                    Log.d(TAG, ex.toString());
                }
            }
        } else {
            textViewSupportWidevine.setText(R.string.text_not_supported);
        }

    }
}
