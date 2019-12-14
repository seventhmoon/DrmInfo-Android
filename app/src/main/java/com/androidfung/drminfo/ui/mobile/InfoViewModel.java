package com.androidfung.drminfo.ui.mobile;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.androidfung.drminfo.R;

import java.util.ArrayList;
import java.util.Arrays;

import static android.view.Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION;
import static android.view.Display.HdrCapabilities.HDR_TYPE_HDR10;
import static android.view.Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS;
import static android.view.Display.HdrCapabilities.HDR_TYPE_HLG;

public class InfoViewModel extends AndroidViewModel {

    private MutableLiveData<ObservableArrayMap<String, Object>> mProperties;
//    protected MutableLiveData<String> mSdkLevel;

    public InfoViewModel(Application application) {
        super(application);
    }

    public LiveData<ObservableArrayMap<String, Object>> getProperties() {
        if (mProperties == null) {
            mProperties = new MutableLiveData<>();
            ObservableArrayMap<String, Object> map = new ObservableArrayMap<>();

//            mProperties = Transformations.map(map, null);

            map.put("sdkLevel", Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT >= 21) {
                String[] supportedAbis = Build.SUPPORTED_ABIS;
                String abis = Arrays.toString(supportedAbis).replace("[","").replace("]","");
                map.put("abis", abis);
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                WindowManager windowManager = (WindowManager) getApplication().getSystemService(Activity.WINDOW_SERVICE);
                Display display = windowManager.getDefaultDisplay();
                Display.HdrCapabilities hdrCapabilities = display.getHdrCapabilities();

                int[] supportedHdrTypes = hdrCapabilities.getSupportedHdrTypes();
                ArrayList<String> supportedHdrTypeStrings = null;
                if (supportedHdrTypes != null){
                    supportedHdrTypeStrings = new ArrayList<>();

                    for(int i : supportedHdrTypes){
                        switch (i){
                            case HDR_TYPE_DOLBY_VISION : supportedHdrTypeStrings.add("Dolby Vision HDR");
                            case HDR_TYPE_HDR10 : supportedHdrTypeStrings.add("HDR10");
                            case HDR_TYPE_HDR10_PLUS : supportedHdrTypeStrings.add("HDR10+");
                            case HDR_TYPE_HLG : supportedHdrTypeStrings.add("Hybrid Log-Gamma HDR");

                        }
                    }
                }else{
                    supportedHdrTypeStrings.add(getApplication().getString(R.string.text_not_supported));
                }

                map.put("supportedHdrTypes", supportedHdrTypeStrings);
            }

            mProperties.setValue(map);
        }

        return mProperties;
    }
}
