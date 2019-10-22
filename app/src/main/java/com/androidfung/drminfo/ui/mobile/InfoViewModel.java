package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.os.Build;

import androidx.databinding.ObservableArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.androidfung.drminfo.R;

import java.util.Arrays;

import static com.androidfung.drminfo.Util.toHdcpLevelString;

public class InfoViewModel extends AndroidViewModel {

    protected MutableLiveData<ObservableArrayMap<String, Object>> mProperties;
//    protected MutableLiveData<String> mSdkLevel;

    protected InfoViewModel(Application application) {
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

            mProperties.setValue(map);
        }

        return mProperties;
    }
}
