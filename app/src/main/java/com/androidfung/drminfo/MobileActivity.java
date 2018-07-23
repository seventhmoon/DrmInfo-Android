package com.androidfung.drminfo;

import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.androidfung.drminfo.databinding.ActivityMobileBinding;
import com.androidfung.drminfo.ui.mobile.SharedViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;
@Keep
public class MobileActivity extends AppCompatActivity {


    private static final String TAG = MobileActivity.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMobileBinding activityMobileBinding = DataBindingUtil.setContentView(this, R.layout.activity_mobile);
        activityMobileBinding.setLifecycleOwner(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        SharedViewModel sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel.class);
        sharedViewModel.getProperties().observe(this, activityMobileBinding::setMap);

    }
}
