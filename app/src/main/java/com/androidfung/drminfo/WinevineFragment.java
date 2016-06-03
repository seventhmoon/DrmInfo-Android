package com.androidfung.drminfo;


import android.media.MediaDrm;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class WinevineFragment extends Fragment {

    private MediaDrm mWidevineMediaDrm;

    public WinevineFragment() {
        // Required empty public constructor
        mWidevineMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);
    }

    @SuppressWarnings("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_winevine, container, false);
        TextView textViewSupportWidevine = (TextView) rootView.findViewById(R.id.textview_support_widevine);
        GridLayout gridLayoutWidevine = (GridLayout) rootView.findViewById(R.id.gridlayout_widevine);

        if (mWidevineMediaDrm != null) {

            textViewSupportWidevine.setText(R.string.text_supported);

//            TextView[] textViews = {textViewWidevineVersion, textViewWidevineSystemId, textViewWidevineSecurityLevel, textViewWidevineHdcpLevel, textViewWidevineMaxHdcpLevel, textViewWidevineUsageReportingSupport, textViewWidevineMaxNumberOfSessions, textViewWidevineNumberOfOpenSessions};
            int[] headerIds = {R.string.text_version, R.string.text_widevine_system_id, R.string.text_widevine_security_level, R.string.text_widevine_hdcp_level, R.string.text_widevine_max_hdcp_level, R.string.text_widevine_usage_reporting_support, R.string.text_widevine_maximum_number_of_sessions, R.string.text_widevine_number_of_open_sessions};
            String[] keys = {MediaDrm.PROPERTY_VERSION, "systemId", "securityLevel", "hdcpLevel", "maxHdcpLevel", "usageReportingSupport", "maxNumberOfSessions", "numberOfOpenSessions"};

            for (int i = 0; i < headerIds.length; i++) {
                try {
                    AppCompatTextView textViewHeader = new AppCompatTextView(getContext());
                    AppCompatTextView textViewValue = new AppCompatTextView(getContext());
//                    textViewValue.setGravity(Gravity.RIGHT);

                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                    layoutParams.setGravity(Gravity.END);
                    textViewValue.setLayoutParams(layoutParams);


                    textViewHeader.setText(headerIds[i]);
                    textViewValue.setText(mWidevineMediaDrm.getPropertyString(keys[i]));

                    if (Build.VERSION.SDK_INT >= 23) {
                        textViewHeader.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                    } else {
                        textViewHeader.setTextAppearance(getContext(), android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
                        textViewValue.setTextAppearance(getContext(), android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Body2);
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
        return rootView;
    }

}
