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

import com.androidfung.drminfo.safetynet.CompatibilityCheckResponse;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.gson.GsonBuilder;

import java.security.SecureRandom;


/**
 * A simple {@link Fragment} subclass.
 */
public class SafetyNetFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    private final String TAG = SafetyNetFragment.class.getSimpleName();
    private View mRootView;
    private GoogleApiClient mGoogleApiClient;

    public SafetyNetFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(SafetyNetFragment.this)
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
        mRootView = inflater.inflate(R.layout.fragment_info, container, false);


        TextView textViewSdkLevel = (TextView) mRootView.findViewById(R.id.textview_sdk_level);
        TextView textViewModel = (TextView) mRootView.findViewById(R.id.textview_model);
        TextView textViewBrand = (TextView) mRootView.findViewById(R.id.textview_brand);
        TextView textViewProduct = (TextView) mRootView.findViewById(R.id.textview_product);
        textViewModel.setText(Build.MODEL);
        textViewSdkLevel.setText(String.valueOf(Build.VERSION.SDK_INT));
        textViewBrand.setText(Build.BRAND);
        textViewProduct.setText(Build.PRODUCT);

        mRootView.post(() ->
            mGoogleApiClient.connect()
        );
        return mRootView;

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

                        try {
                            String[] jwsResults = result.getJwsResult().split("\\.");
                            String decodedPayload = new String(Base64.decode(jwsResults[1], Base64.DEFAULT));
                            Log.d(TAG, decodedPayload);
                            CompatibilityCheckResponse compatibilityCheckResponse = new GsonBuilder().create().fromJson(decodedPayload, CompatibilityCheckResponse.class);
                            TextView textViewCtsProfileMatch = (TextView) mRootView.findViewById(R.id.textview_cts_profile_match);
                            textViewCtsProfileMatch.setText(compatibilityCheckResponse.isCtsProfileMatch()?R.string.text_yes:R.string.text_no);

                        }catch (Exception e){}
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
