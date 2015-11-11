package com.androidfung.drminfo;

import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.UUID;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public static final UUID WIDEVINE_UUID = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);
    public static final UUID PLAYREADY_UUID = new UUID(0x9A04F07998404286L, 0xAB92E65BE0885F95L);

    private MediaDrm mWidevineMediaDrm, mPlayreadyMediaDrm;

    public MainActivityFragment() {

    }

    @SuppressWarnings("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        TextView textViewSupportPlayready = (TextView) rootView.findViewById(R.id.textview_support_playready);
        TextView textViewSupportWidevine = (TextView) rootView.findViewById(R.id.textview_support_widevine);

        try {
            mWidevineMediaDrm = new MediaDrm(WIDEVINE_UUID);
            mPlayreadyMediaDrm = new MediaDrm(PLAYREADY_UUID);
        } catch (UnsupportedSchemeException e) {
            e.printStackTrace();
        }

        if (mWidevineMediaDrm != null) {
            textViewSupportWidevine.setText(getString(R.string.text_supported) + " (" + mWidevineMediaDrm.getPropertyString("securityLevel") + ")");
        } else {
            textViewSupportWidevine.setText(R.string.text_not_supported);
        }

        if (mPlayreadyMediaDrm != null) {
            textViewSupportPlayready.setText(R.string.text_supported);
        } else {
            textViewSupportPlayready.setText(R.string.text_not_supported);
        }

        return rootView;
    }
}
