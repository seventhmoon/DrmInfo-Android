package com.androidfung.drminfo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableArrayMap;
import androidx.databinding.ViewDataBinding;

import android.drm.DrmInfo;
import android.media.MediaDrm;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static android.media.MediaDrm.SECURITY_LEVEL_HW_SECURE_ALL;


/**
 * A simple {@link Fragment} subclass.
 */
@Keep
public class InfoFragment extends androidx.fragment.app.Fragment {

    private final String TAG = InfoFragment.class.getSimpleName();
//    private View mRootView;

    public InfoFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewDataBinding  binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info, container, false);
        View rootView = binding.getRoot();
//        mRootView = inflater.inflate(R.layout.fragment_info, container, false);

//        TextView textViewSdkLevel = (TextView) mRootView.findViewById(R.id.textview_sdk_level);
//        TextView textViewModel = (TextView) mRootView.findViewById(R.id.textview_model);
//        TextView textViewBrand = (TextView) mRootView.findViewById(R.id.textview_brand);
//        TextView textViewProduct = (TextView) mRootView.findViewById(R.id.textview_product);
//        textViewModel.setText(Build.MODEL);
//        textViewSdkLevel.setText(String.valueOf(Build.VERSION.SDK_INT));
//        textViewBrand.setText(Build.BRAND);
//        textViewProduct.setText(Build.PRODUCT);

        ObservableArrayMap<String, Object> map = new ObservableArrayMap<>();

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
//            int maxSecurityLevel = MediaDrm.getMaxSecurityLevel();
//
//            Log.d(TAG, "maxSecurityLevel: " + maxSecurityLevel);
//            if (maxSecurityLevel == MediaDrm.SECURITY_LEVEL_HW_SECURE_ALL) {
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_HW_SECURE_ALL");
//            }else if (maxSecurityLevel == MediaDrm.SECURITY_LEVEL_HW_SECURE_DECODE){
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_HW_SECURE_DECODE");
//            }else if (maxSecurityLevel == MediaDrm.SECURITY_LEVEL_HW_SECURE_CRYPTO){
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_HW_SECURE_CRYPTO");
//            }else if (maxSecurityLevel == MediaDrm.SECURITY_LEVEL_SW_SECURE_DECODE){
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_SW_SECURE_DECODE");
//            }else if (maxSecurityLevel == MediaDrm.SECURITY_LEVEL_SW_SECURE_CRYPTO){
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_SW_SECURE_CRYPTO");
//            }else if (maxSecurityLevel == MediaDrm.SECURITY_LEVEL_UNKNOWN) {
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_UNKNOWN");
//            }else {
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_UNKNOWN");
//            }
//        }



        PackageManager pm = getContext().getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo("com.google.android.webview", 0);
//            if (pi != null){
            map.put("versionCode", pi.versionCode);
            map.put("versionName", pi.versionName);
//            }
//            Log.d(TAG, "version name: " + pi.versionName);
//            Log.d(TAG, "version code: " + pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Android System WebView is not found");
        }

        binding.setVariable(com.androidfung.drminfo.BR.map, map);
        return rootView;

    }

}
