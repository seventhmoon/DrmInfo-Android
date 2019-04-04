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
import java.util.UUID;

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

        return subActions;
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {

        //info section here
        int[] infoTitles = {R.string.text_brand, R.string.text_product, R.string.text_model, R.string.text_sdk_level};
        String[] descriptions = {Build.BRAND, Build.PRODUCT, Build.MODEL, String.valueOf(Build.VERSION.SDK_INT)};

        for (int i=0; i<infoTitles.length; i++){
            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(true)
                    .title(getString(infoTitles[i]))
                    .description(descriptions[i])
                    .build());
        }

        UUID[] uuids = {Constants.CLEARKEY_UUID, Constants.WIDEVINE_UUID, Constants.PLAYREADY_UUID, Constants.VERIMATRIX_UUID, Constants.MARLIN_UUID, Constants.PRIMETIME_UUID};
        int[] titles = {R.string.text_clearkey_name, R.string.text_widevine_name, R.string.text_playready_name, R.string.text_verimatrix_name, R.string.text_marlin_name, R.string.text_primetime_name};

        for (int i=0; i < uuids.length; i++){
            boolean supported = MediaDrm.isCryptoSchemeSupported(uuids[i]);
            List<GuidedAction> subActions = null;
            if (supported){
                try  {
                    MediaDrm mediaDrm = new MediaDrm(uuids[i]);
                    if (Constants.WIDEVINE_UUID == uuids[i]){
                        subActions = getWidevineSubActions(mediaDrm);
                    }else {
                        subActions = getMediaDrmSubActions(mediaDrm);
                    }


                } catch (UnsupportedSchemeException e) {
                    e.printStackTrace();
                }

            }

            actions.add(new GuidedAction.Builder(getActivity())
                    .infoOnly(subActions == null || subActions.isEmpty())
                    .subActions((subActions == null || subActions.isEmpty() )? null: subActions)
                    .title(getString(titles[i]))
                    .description(supported ? getString(R.string.text_supported) : getString(R.string.text_not_supported))
                    .build());
        }
    }

}