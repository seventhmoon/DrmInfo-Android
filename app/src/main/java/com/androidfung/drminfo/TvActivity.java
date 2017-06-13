package com.androidfung.drminfo;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.MediaDrm;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
@Keep
public class TvActivity extends Activity {

    private static final String TAG = TvActivity.class.getSimpleName();
    private static final long WIDEVINE = 2L;

    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            GuidedStepFragment.addAsRoot(this, new InfoStepFragment(), android.R.id.content);
        }
// Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, new Bundle());
//        // Get tracker.
//        Tracker t = ((MainApplication) getApplication()).getTracker();
//
//        // Set screen name.
//        t.setScreenName(TAG);
//
//        // Send a screen view.
//        t.send(new HitBuilders.ScreenViewBuilder().build());


    }


    public static class InfoStepFragment extends GuidedStepFragment {
        private MediaDrm mWidevineMediaDrm, mPlayreadyMediaDrm, mMarlinMediaDrm, mVerimatrixMediaDrm, mClearKeyMediaDrm;
//        InterstitialAd mInterstitialAd;

        @Override
        public int onProvideTheme() {
            return super.onProvideTheme();
        }

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.app_name);
            String description = null;
            String breadcrumb = null;
            Drawable icon = null;

            return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
        }

        @SuppressWarnings("ResourceType")
        private List<GuidedAction> getPlayReadySubActions() {
            List<GuidedAction> subActions = new ArrayList<>();
            try {
                String version = mPlayreadyMediaDrm.getPropertyString(MediaDrm.PROPERTY_VERSION);
                if (version != null) {
                    subActions.add(new GuidedAction.Builder(getActivity())
                            .infoOnly(true)
                            .title(getString(R.string.text_version))
                            .description(version)
                            .build());
                }
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
                if (version != null) {
                    subActions.add(new GuidedAction.Builder(getActivity())
                            .infoOnly(true)
                            .title(getString(R.string.text_version))
                            .description(version)
                            .build());
                }
            } catch (java.lang.IllegalStateException ex) {
                Log.d(TAG, ex.toString());
            }

            return subActions;
        }

        @SuppressWarnings("ResourceType")
        private List<GuidedAction> getWidevineSubActions() {
            List<GuidedAction> subActions = new ArrayList<>();
            if (mWidevineMediaDrm != null) {
                try {
                    String securityLevel = mWidevineMediaDrm.getPropertyString("securityLevel");
                    if (securityLevel != null) {
                        subActions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_security_level))
                                .description(securityLevel)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }

                try {
                    String systemId = mWidevineMediaDrm.getPropertyString("systemId");
                    if (systemId != null) {
                        subActions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_system_id))
                                .description(systemId)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }


                try {
                    String usageReportingSupport = mWidevineMediaDrm.getPropertyString("usageReportingSupport");
                    if (usageReportingSupport != null) {
                        subActions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_usage_reporting_support))
                                .description(usageReportingSupport)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }

                try {
                    String hdcpLevel = mWidevineMediaDrm.getPropertyString("hdcpLevel");
                    if (hdcpLevel != null) {
                        subActions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_hdcp_level))
                                .description(hdcpLevel)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }

                try {
                    String maxHdcpLevel = mWidevineMediaDrm.getPropertyString("maxHdcpLevel");
                    if (maxHdcpLevel != null) {
                        subActions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_max_hdcp_level))
                                .description(maxHdcpLevel)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }


                try {
                    String maxNumberOfSessions = mWidevineMediaDrm.getPropertyString("maxNumberOfSessions");
                    if (maxNumberOfSessions != null) {
                        subActions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_maximum_number_of_sessions))
                                .description(maxNumberOfSessions)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }

                try {
                    String numberOfOpenSessions = mWidevineMediaDrm.getPropertyString("numberOfOpenSessions");
                    if (numberOfOpenSessions != null) {
                        subActions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_number_of_open_sessions))
                                .description(numberOfOpenSessions)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
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

//        @Override
//        public void onGuidedActionClicked(GuidedAction action) {
//
//            if (action.getId() == WIDEVINE) {
//                    FragmentManager fm = getFragmentManager();
//                    GuidedStepFragment.add(fm, new WidevineStepFragment());
//            }
//        }

    }

    @Deprecated
    public static class WidevineStepFragment extends GuidedStepFragment {
        private MediaDrm mWidevineMediaDrm;

        @Override
        public int onProvideTheme() {
            return super.onProvideTheme();
        }

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.text_widevine_name);
            String description = null;
            String breadcrumb = getString(R.string.app_name);
            Drawable icon = null;

            return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
        }

        @SuppressWarnings("ResourceType")
        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            mWidevineMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);

            if (mWidevineMediaDrm != null) {
                try {
                    String securityLevel = mWidevineMediaDrm.getPropertyString("securityLevel");
                    if (securityLevel != null) {
                        actions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_security_level))
                                .description(securityLevel)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }

                try {
                    String systemId = mWidevineMediaDrm.getPropertyString("systemId");
                    if (systemId != null) {
                        actions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_system_id))
                                .description(systemId)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }


                try {
                    String usageReportingSupport = mWidevineMediaDrm.getPropertyString("usageReportingSupport");
                    if (usageReportingSupport != null) {
                        actions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_usage_reporting_support))
                                .description(usageReportingSupport)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }

                try {
                    String hdcpLevel = mWidevineMediaDrm.getPropertyString("hdcpLevel");
                    if (hdcpLevel != null) {
                        actions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_hdcp_level))
                                .description(hdcpLevel)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }

                try {
                    String maxHdcpLevel = mWidevineMediaDrm.getPropertyString("maxHdcpLevel");
                    if (maxHdcpLevel != null) {
                        actions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_max_hdcp_level))
                                .description(maxHdcpLevel)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }


                try {
                    String maxNumberOfSessions = mWidevineMediaDrm.getPropertyString("maxNumberOfSessions");
                    if (maxNumberOfSessions != null) {
                        actions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_maximum_number_of_sessions))
                                .description(maxNumberOfSessions)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }

                try {
                    String numberOfOpenSessions = mWidevineMediaDrm.getPropertyString("numberOfOpenSessions");
                    if (numberOfOpenSessions != null) {
                        actions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(getString(R.string.text_widevine_number_of_open_sessions))
                                .description(numberOfOpenSessions)
                                .build());
                    }
                } catch (java.lang.IllegalStateException ex) {
                    Log.d(TAG, ex.toString());
                }
            }

        }
    }

}

