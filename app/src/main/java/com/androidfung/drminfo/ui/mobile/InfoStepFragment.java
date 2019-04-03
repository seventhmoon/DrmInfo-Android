package com.androidfung.drminfo.ui.mobile;

import android.graphics.drawable.Drawable;
import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.androidfung.drminfo.Constants;
import com.androidfung.drminfo.MediaDrmFactory;
import com.androidfung.drminfo.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import static com.androidfung.drminfo.Constants.PRIMETIME_UUID;
import static com.androidfung.drminfo.Constants.VERIMATRIX_UUID;

public class InfoStepFragment extends GuidedStepSupportFragment {

    private static final String TAG = InfoStepFragment.class.getSimpleName();

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.app_name);
        String description = getString(R.string.text_on_android_tv);
        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher_round, null);

        return new GuidanceStylist.Guidance(title, description, null, icon);
    }


    @SuppressWarnings("ResourceType")
    private List<GuidedAction> getMediaDrmSubActions(@NonNull MediaDrm mediaDrm) {
        List<GuidedAction> subActions = new ArrayList<>();

        String[] drmStringKeys = getResources().getStringArray(R.array.mediadrm_string_property_keys);
        String[] drmStringNames = getResources().getStringArray(R.array.mediadrm_string_property_names);
        for (int i = 0; i < drmStringKeys.length; i++) {
            try {
                String value = mediaDrm.getPropertyString(drmStringKeys[i]);
                String name = drmStringNames[i];

                if (!TextUtils.isEmpty(value)) {
                    subActions.add(new GuidedAction.Builder(getActivity())
                            .infoOnly(true)
                            .title(name)
                            .description(value)
                            .build());
                }
            } catch (IllegalStateException | IllegalArgumentException ex) {
                Log.d(TAG, ex.toString());
            }
        }

        return subActions;
    }

    @SuppressWarnings("ResourceType")
    private List<GuidedAction> getWidevineSubActions(@NonNull MediaDrm widevineDrm) {
        List<GuidedAction> subActions = getMediaDrmSubActions(widevineDrm);

        String[] wvStringKeys = getResources().getStringArray(R.array.widevine_string_property_keys);
        String[] wvStringNames = getResources().getStringArray(R.array.widevine_string_property_names);

        for (int i = 0; i < wvStringKeys.length; i++) {
            try {
                String value = widevineDrm.getPropertyString(wvStringKeys[i]);
                if (!TextUtils.isEmpty(value)) {
                    subActions.add(new GuidedAction.Builder(getActivity())
                            .infoOnly(true)
                            .title(wvStringNames[i])
                            .description(value)
                            .build());
                }
            } catch (IllegalStateException | IllegalArgumentException ex) {
                Log.d(TAG, ex.toString());
            }
        }
//        }

        return subActions;
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {

        //info section here
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

        boolean verimatrixSupported = MediaDrm.isCryptoSchemeSupported(VERIMATRIX_UUID);
        boolean primetimeSupported = MediaDrm.isCryptoSchemeSupported(PRIMETIME_UUID);
        boolean marlinSupported = MediaDrm.isCryptoSchemeSupported(Constants.MARLIN_UUID);
        boolean clearKeySupported = MediaDrm.isCryptoSchemeSupported(Constants.CLEARKEY_UUID);
        boolean playreadySupported = MediaDrm.isCryptoSchemeSupported(Constants.PLAYREADY_UUID);
        boolean widevineSupported = MediaDrm.isCryptoSchemeSupported(Constants.WIDEVINE_UUID);

        if (clearKeySupported) {
            try {
                MediaDrm clearKeyMediaDrm = new MediaDrm(Constants.CLEARKEY_UUID);

                List<GuidedAction> subActions = getWidevineSubActions(clearKeyMediaDrm);

                actions.add(new GuidedAction.Builder(getActivity())
                        .infoOnly(subActions.isEmpty())
                        .title(getString(R.string.text_clearkey_name))
                        .description(getString(R.string.text_supported))
                        .subActions(subActions.isEmpty() ? null : subActions)
                        .build());
            } catch (UnsupportedSchemeException e) {
                e.printStackTrace();
            }
        } else {
            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_clearkey_name))
                    .description(getString(R.string.text_not_supported))
                    .build());
        }

        if (widevineSupported) {
            try {
                MediaDrm widevineDrm = new MediaDrm(Constants.WIDEVINE_UUID);

                List<GuidedAction> widevineSubActions = getWidevineSubActions(widevineDrm);
                actions.add(new GuidedAction.Builder(getActivity())
//                        .id(WIDEVINE)
                        .title(getString(R.string.text_widevine_name))
                        .description(getString(R.string.text_supported))
                        .subActions(widevineSubActions.isEmpty() ? null : widevineSubActions)
                        .build());

            } catch (UnsupportedSchemeException e) {
                e.printStackTrace();
            }
        } else {
            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_widevine_name))
                    .description(getString(R.string.text_not_supported))
                    .build());
        }

        if (playreadySupported) {
            try {
                MediaDrm playReadyDrm = new MediaDrm(Constants.PLAYREADY_UUID);

                List<GuidedAction> playreadySubActions = getMediaDrmSubActions(playReadyDrm);
                actions.add(new GuidedAction.Builder(getActivity())
                        .infoOnly(playreadySubActions.isEmpty())
                        .title(getString(R.string.text_playready_name))
                        .description(getString(R.string.text_supported))
                        .subActions(playreadySubActions.isEmpty() ? null : playreadySubActions)
                        .build());
            } catch (UnsupportedSchemeException e) {
                e.printStackTrace();
            }
        } else {
            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_playready_name))
                    .description(getString(R.string.text_not_supported))
                    .build());
        }

        int[] titles = {R.string.text_verimatrix_name, R.string.text_marlin_name, R.string.text_primetime_name};
        boolean[] supportingStatus = {verimatrixSupported, marlinSupported, primetimeSupported};

        for (int i=0; i < supportingStatus.length; i++){
            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(titles[i]))
                    .description(supportingStatus[i] ? getString(R.string.text_supported) : getString(R.string.text_not_supported))
                    .build());
        }
    }

}