package com.androidfung.drminfo.ui.mobile;

import android.app.Application;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class InfoViewModel extends AndroidViewModel {

    private final static String TAG = InfoViewModel.class.getSimpleName();
    protected MutableLiveData<ObservableArrayMap<String, Object>> mProperties;

    public InfoViewModel(Application application) {
        super(application);
    }

    public LiveData<ObservableArrayMap<String, Object>> getProperties(){
        if (mProperties == null){
            mProperties = new MutableLiveData<>();
        }

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
//            int maxSecurityLevel = MediaDrm.getMaxSecurityLevel();
//
//            Log.d(TAG, "maxSecurityLevel: " + maxSecurityLevel);
//            if (maxSecurityLevel > MediaDrm.SECURITY_LEVEL_HW_SECURE_ALL) {
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_HW_SECURE_ALL");
//            }else if (maxSecurityLevel > MediaDrm.SECURITY_LEVEL_HW_SECURE_DECODE){
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_HW_SECURE_DECODE");
//            }else if (maxSecurityLevel > MediaDrm.SECURITY_LEVEL_HW_SECURE_CRYPTO){
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_HW_SECURE_CRYPTO");
//            }else if (maxSecurityLevel > MediaDrm.SECURITY_LEVEL_SW_SECURE_DECODE){
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_SW_SECURE_DECODE");
//            }else if (maxSecurityLevel > MediaDrm.SECURITY_LEVEL_SW_SECURE_CRYPTO){
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_SW_SECURE_CRYPTO");
//            }else if (maxSecurityLevel > MediaDrm.SECURITY_LEVEL_UNKNOWN) {
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_UNKNOWN");
//            }else {
//                map.put("maxSecurityLevel", "SECURITY_LEVEL_UNKNOWN");
//            }
//        }


//        PackageManager pm = getContext().getPackageManager();
//        try {
//            PackageInfo pi = pm.getPackageInfo("com.google.android.webview", 0);
//            map.put("versionCode", pi.versionCode);
//            map.put("versionName", pi.versionName);
//
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "Android System WebView is not found");
//        }


        return mProperties;
    }



}
