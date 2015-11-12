package com.androidfung.drminfo;

import android.app.Activity;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TvActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            GuidedStepFragment.add(getFragmentManager(), new InfoGuidedStepFragment());
        }
    }

    @Override
    public void onBackPressed() {
        // your code.
        finish();
    }
}

