package com.androidfung.drminfo;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.androidfung.drminfo.databinding.ActivityMobileBinding;
import com.androidfung.drminfo.ui.mobile.SharedViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public void displayLicenseInformation(MenuItem item) {
        startActivity(new Intent(this, OssLicensesMenuActivity.class));
    }
}
