package com.androidfung.drminfo;

import android.os.Bundle;

import com.androidfung.drminfo.ui.mobile.InfoStepFragment;
import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.annotation.Keep;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
@Keep
public class TvActivity extends FragmentActivity {

    private static final String TAG = TvActivity.class.getSimpleName();
//    private static final long WIDEVINE = 2L;

    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            GuidedStepSupportFragment.addAsRoot(this, new InfoStepFragment(), android.R.id.content);
        }
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    }

}

