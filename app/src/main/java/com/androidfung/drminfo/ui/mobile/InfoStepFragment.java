package com.androidfung.drminfo.ui.mobile;

import android.graphics.drawable.Drawable;
import android.media.MediaDrm;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.androidfung.drminfo.MediaDrmFactory;
import com.androidfung.drminfo.R;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

public class InfoStepFragment extends GuidedStepSupportFragment {

    private static final String TAG = InfoStepFragment.class.getSimpleName();

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.app_name);
        String description = getString(R.string.text_on_android_tv);

        Drawable icon = ContextCompat.getDrawable(getActivity(), R.mipmap.ic_launcher_round);

        return new GuidanceStylist.Guidance(title, description, null, icon);
    }


    @SuppressWarnings("ResourceType")
    private List<GuidedAction> getMediaDrmSubActions(@NonNull MediaDrm mediaDrm) {
        List<GuidedAction> subActions = new ArrayList<>();

        String[] drmStringKeys = getResources().getStringArray(R.array.mediadrm_string_property_keys);
        String[] drmStringNames = getResources().getStringArray(R.array.mediadrm_string_property_names);
        for (int i=0; i< drmStringKeys.length; i++){
            try {
                String value = mediaDrm.getPropertyString(drmStringKeys[i]);
                if (!Strings.isNullOrEmpty(value)) {
                    subActions.add(new GuidedAction.Builder(getActivity())
                            .infoOnly(true)
                            .title(drmStringNames[i])
                            .description(value)
                            .build());
                }
            } catch (IllegalStateException ex) {
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

            for (int i=0; i< wvStringKeys.length; i++){
                try {
                    String value = widevineDrm.getPropertyString(wvStringKeys[i]);
                    if (!Strings.isNullOrEmpty(value)) {
                        subActions.add(new GuidedAction.Builder(getActivity())
                                .infoOnly(true)
                                .title(wvStringNames[i])
                                .description(value)
                                .build());
                    }
                } catch (IllegalStateException ex) {
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


        MediaDrm widevineMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);
        MediaDrm playreadyMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PLAYREADY);
        MediaDrm marlinMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.MARLIN);
        MediaDrm verimatrixMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.VERIMATRIX);
        MediaDrm clearKeyMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.CLEARKEY);
        MediaDrm primetimeMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.PRIMETIME);



        if (clearKeyMediaDrm != null) {

            actions.add(new GuidedAction.Builder(getActivity())
//                    .infoOnly(true)
                    .title(getString(R.string.text_clearkey_name))
                    .description(getString(R.string.text_supported))
                    .subActions(getMediaDrmSubActions(clearKeyMediaDrm))
                    .build());

        } else {
            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_clearkey_name))
                    .description(getString(R.string.text_not_supported))
                    .build());
        }

        if (widevineMediaDrm != null) {

            actions.add(new GuidedAction.Builder(getActivity())
//                        .id(WIDEVINE)
                    .title(getString(R.string.text_widevine_name))
                    .description(getString(R.string.text_supported))
                    .subActions(getWidevineSubActions(widevineMediaDrm))
                    .build());

        } else {
            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(R.string.text_widevine_name))
                    .description(getString(R.string.text_not_supported))
                    .build());
        }

        if (playreadyMediaDrm != null) {

            actions.add(new GuidedAction.Builder(getActivity())
//                    .infoOnly(true)
                    .title(getString(R.string.text_playready_name))
                    .description(getString(R.string.text_supported))
                    .subActions(getMediaDrmSubActions(playreadyMediaDrm))
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
                .description(primetimeMediaDrm != null ? getString(R.string.text_supported) : getString(R.string.text_not_supported))
                .build());

        actions.add(new GuidedAction.Builder(getActivity())
                .infoOnly(true)
                .title(getString(R.string.text_marlin_name))
                .description(marlinMediaDrm != null ? getString(R.string.text_supported) : getString(R.string.text_not_supported))
                .build());

        actions.add(new GuidedAction.Builder(getActivity())
                .infoOnly(true)
                .title(getString(R.string.text_verimatrix_name))
                .description(verimatrixMediaDrm != null ? getString(R.string.text_supported) : getString(R.string.text_not_supported))
                .build());


    }

}