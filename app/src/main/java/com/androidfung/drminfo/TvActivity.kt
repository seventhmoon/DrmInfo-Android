package com.androidfung.drminfo

import android.os.Bundle
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment
import com.androidfung.drminfo.ui.mobile.InfoStepFragment
import com.google.firebase.analytics.FirebaseAnalytics

@Keep
class TvActivity : FragmentActivity() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (null == savedInstanceState) {
            GuidedStepSupportFragment.addAsRoot(this, InfoStepFragment(), R.id.content)
        }
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    companion object {
        private val TAG = TvActivity::class.java.simpleName
    }
}