package com.androidfung.drminfo

import android.os.Bundle
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.leanback.app.GuidedStepSupportFragment
import com.androidfung.drminfo.ui.mobile.InfoStepFragment
import com.google.firebase.analytics.FirebaseAnalytics

@Keep
class TvActivity : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            GuidedStepSupportFragment.addAsRoot(this, InfoStepFragment(), android.R.id.content)
        }

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    companion object {
        private val TAG = TvActivity::class.java.simpleName
    }
}