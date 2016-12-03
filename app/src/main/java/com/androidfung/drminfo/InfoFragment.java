package com.androidfung.drminfo;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;

import java.security.SecureRandom;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    private final String TAG = InfoFragment.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;

    public InfoFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(InfoFragment.this)
                .build();
    }

    private byte[] getRequestNonce() {
        byte[] output = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(output);
        return output;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);


        TextView textViewSdkLevel = (TextView) rootView.findViewById(R.id.textview_sdk_level);
        TextView textViewModel = (TextView) rootView.findViewById(R.id.textview_model);
        TextView textViewBrand = (TextView) rootView.findViewById(R.id.textview_brand);
        TextView textViewProduct = (TextView) rootView.findViewById(R.id.textview_product);
        textViewModel.setText(Build.MODEL);
        textViewSdkLevel.setText(String.valueOf(Build.VERSION.SDK_INT));
        textViewBrand.setText(Build.BRAND);
        textViewProduct.setText(Build.PRODUCT);


        return rootView;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        byte[] nonce = getRequestNonce(); // Should be at least 16 bytes in length.
        SafetyNet.SafetyNetApi.attest(mGoogleApiClient, nonce)
                .setResultCallback(result -> {
                    Status status = result.getStatus();
                    if (status.isSuccess()) {
                        // Indicates communication with the service was successful.
                        // result.getJwsResult() contains the result data

                        String jwsResult = result.getJwsResult();
                        Log.d(TAG, jwsResult);

                    } else {
                        // An error occurred while communicating with the service
                        Log.d(TAG, result.toString());
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
