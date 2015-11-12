package com.androidfung.drminfo;

import android.app.Activity;
import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;


public class InfoGuidedStepFragment extends GuidedStepFragment {


    public static final UUID WIDEVINE_UUID = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);
    public static final UUID PLAYREADY_UUID = new UUID(0x9A04F07998404286L, 0xAB92E65BE0885F95L);
    private MediaDrm mWidevineMediaDrm, mPlayreadyMediaDrm;

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.app_name), null, null, null);
    }



    @SuppressWarnings("ResourceType")
    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        try {
            mWidevineMediaDrm = new MediaDrm(WIDEVINE_UUID);
            mPlayreadyMediaDrm = new MediaDrm(PLAYREADY_UUID);
        } catch (UnsupportedSchemeException e) {
            e.printStackTrace();
        }

        actions.add(new GuidedAction.Builder()
                .infoOnly(true)
                .title(getString(R.string.text_brand))
                .description(Build.BRAND)
                .build());

        actions.add(new GuidedAction.Builder()
                .infoOnly(true)
                .title(getString(R.string.text_product))
                .description(Build.PRODUCT)
                .build());

        actions.add(new GuidedAction.Builder()
                .infoOnly(true)
                .title(getString(R.string.text_model))
                .description(Build.MODEL)
                .build());

        actions.add(new GuidedAction.Builder()
                .infoOnly(true)
                .title(getString(R.string.text_sdk_level))
                .description(String.valueOf(Build.VERSION.SDK_INT))
                .build());

        actions.add(new GuidedAction.Builder()
                .infoOnly(true)
                .title(getString(R.string.text_widevine_name))
                .description(mWidevineMediaDrm != null ? getString(R.string.text_supported_with_security_level, mWidevineMediaDrm.getPropertyString("securityLevel")): getString(R.string.text_not_supported))
                .build());

        actions.add(new GuidedAction.Builder()
                .infoOnly(true)
                .title(getString(R.string.text_playready_name))
                .description(mPlayreadyMediaDrm != null ? getString(R.string.text_supported) : getString(R.string.text_not_supported))
                .build());
    }



}
