package com.androidfung.drminfo;

import android.graphics.drawable.Drawable;
import android.media.MediaDrm;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
@Keep
public class TvActivity extends FragmentActivity {

    private static final String TAG = TvActivity.class.getSimpleName();
//    private static final long WIDEVINE = 2L;

    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            GuidedStepSupportFragment.addAsRoot(this, new InfoStepFragment(), android.R.id.content);
        }
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    }


    public static class InfoStepFragment extends GuidedStepSupportFragment {
        private MediaDrm mWidevineMediaDrm, mPlayreadyMediaDrm, mMarlinMediaDrm, mVerimatrixMediaDrm, mClearKeyMediaDrm, mPrimetimeMediaDrm;

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.app_name);
            String description = getString(R.string.text_on_android_tv);

            Drawable icon = ContextCompat.getDrawable(getActivity(), R.mipmap.ic_launcher_round);

            return new GuidanceStylist.Guidance(title, description, null, icon);
        }

        @SuppressWarnings("ResourceType")
        private List<GuidedAction> getPlayReadySubActions() {
            List<GuidedAction> subActions = new ArrayList<>();
            try {
                String version = mPlayreadyMediaDrm.getPropertyString(MediaDrm.PROPERTY_VERSION);
                    subActions.add(new GuidedAction.Builder(getActivity())
                            .infoOnly(true)
                            .title(getString(R.string.text_version))
                            .description(version)
                            .build());
            } catch (java.lang.IllegalStateException ex) {
                Log.d(TAG, ex.toString());
            }

            return subActions;
        }

        @SuppressWarnings("ResourceType")
        private List<GuidedAction> getClearKeySubActions() {
            List<GuidedAction> subActions = new ArrayList<>();
            try {
                String version = mClearKeyMediaDrm.getPropertyString(MediaDrm.PROPERTY_VERSION);
                    subActions.add(new GuidedAction.Builder(getActivity())
                            .infoOnly(true)
                            .title(getString(R.string.text_version))
                            .description(version)
                            .build());
            } catch (java.lang.IllegalStateException ex) {
                Log.d(TAG, ex.toString());
            }

            return subActions;
        }

        @SuppressWarnings("ResourceType")
        private List<GuidedAction> getWidevineSubActions() {
            List<GuidedAction> subActions = new ArrayList<>();
            String[] propertyStrings = {"securityLevel", "systemId", "usageReportingSupport", "hdcpLevel", "maxHdcpLevel", "maxNumberOfSessions", "numberOfOpenSessions"};
            int[] titles = {R.string.text_widevine_security_level, R.string.text_widevine_system_id, R.string.text_widevine_usage_reporting_support, R.string.key_widevine_hdcp_level,
            R.string.text_widevine_max_hdcp_level, R.string.text_widevine_max_number_of_sessions, R.string.text_widevine_number_of_open_sessions};

            if (mWidevineMediaDrm != null){

                for (int i=0; i< propertyStrings.length; i++){
                    try {
                        String securityLevel = mWidevineMediaDrm.getPropertyString(propertyStrings[i]);
                        subActions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(titles[i]))
                                .description(securityLevel)
                                .build());
                    } catch (java.lang.IllegalStateException ex) {
                        Log.d(TAG, ex.toString());
                    }
                }

            }
            return subActions;
        }

        @SuppressWarnings("ResourceType")
        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {

            mWidevineMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);
            mPlayreadyMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PLAYREADY);
            mMarlinMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.MARLIN);
            mVerimatrixMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.VERIMATRIX);
            mClearKeyMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.CLEARKEY);
            mPrimetimeMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PRIMETIME);

            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_brand))
                    .description(Build.BRAND)
                    .build());

            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_product))
                    .description(Build.PRODUCT)
                    .build());

            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_model))
                    .description(Build.MODEL)
                    .build());

            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_sdk_level))
                    .description(String.valueOf(Build.VERSION.SDK_INT))
                    .build());

            if (mClearKeyMediaDrm != null) {

                actions.add(new GuidedAction.Builder(getActivity())
                        .infoOnly(true)
                        .title(getString(R.string.text_clearkey_name))
                        .description(getString(R.string.text_supported))
//                        .subActions(getPlayReadySubActions())
                        .build());

            } else {
                actions.add(new GuidedAction.Builder(getActivity())
                        .infoOnly(true)
                        .title(getString(R.string.text_clearkey_name))
                        .description(getString(R.string.text_not_supported))
                        .build());
            }

            if (mWidevineMediaDrm != null) {

                actions.add(new GuidedAction.Builder(getActivity())
//                        .id(WIDEVINE)
                        .title(getString(R.string.text_widevine_name))
                        .description(getString(R.string.text_supported))
                        .subActions(getWidevineSubActions())
                        .build());

            } else {
                actions.add(new GuidedAction.Builder(getActivity())
                        .infoOnly(true)
                        .title(getString(R.string.text_widevine_name))
                        .description(getString(R.string.text_not_supported))
                        .build());
            }

            if (mPlayreadyMediaDrm != null) {

                actions.add(new GuidedAction.Builder(getActivity())
                        .infoOnly(true)
                        .title(getString(R.string.text_playready_name))
                        .description(getString(R.string.text_supported))
//                        .subActions(getPlayReadySubActions())
                        .build());

            } else {
                actions.add(new GuidedAction.Builder(getActivity())
                        .infoOnly(true)
                        .title(getString(R.string.text_playready_name))
                        .description(getString(R.string.text_not_supported))
                        .build());
            }

            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_primetime_name))
                    .description(mPrimetimeMediaDrm != null ? getString(R.string.text_supported) : getString(R.string.text_not_supported))
                    .build());

            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_marlin_name))
                    .description(mMarlinMediaDrm != null ? getString(R.string.text_supported) : getString(R.string.text_not_supported))
                    .build());

            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_verimatrix_name))
                    .description(mVerimatrixMediaDrm != null ? getString(R.string.text_supported) : getString(R.string.text_not_supported))
                    .build());
        }

    }

}

